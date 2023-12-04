/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common.web.api.base;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.enums.ContentType;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.TempSm2Cache;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractWithFilesApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Zane
 */
public abstract class AbstractApi<In extends AbstractApiInput, Out> {

    /**
     * The concurrency of the API, used to limit the concurrency of the API
     */
    private static final Map<String, LongAdder> API_PARALLELISM = new ConcurrentHashMap<>();

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());


    /**
     * Concrete implementation of API
     */
    protected abstract ApiResult<Out> handle(In input) throws Exception;

    public ApiResult<Out> execute(String method, JSONObject requestParams) {
        return execute(method, requestParams, null, null);
    }

    public ApiResult<Out> execute(String method, JSONObject requestParams, HttpServletRequest request) {
        return execute(method, requestParams, request, null);
    }

    /**
     * To perform this API
     *
     * @param method        Request way：post/get/...
     * @param requestParams The ginseng
     * @param files         File list
     */
    public ApiResult<Out> execute(String method, JSONObject requestParams, HttpServletRequest request, MultiValueMap<String, MultipartFile> files) {

        String apiClassName = this.getClass().getSimpleName();

        // Checking concurrency Limits
        if (!checkParallelism(apiClassName)) {
            return fail("此接口并发量达到访问上限，请稍后重试。");
        }

        LongAdder apiRunningCount = API_PARALLELISM.get(apiClassName);

        try {
            apiRunningCount.increment();

            // Create the input parameter object
            In apiInput = getInput(method, requestParams, request);

            // Add files
            if (apiInput instanceof AbstractWithFilesApiInput) {
                ((AbstractWithFilesApiInput) apiInput).files = files;
            }

            // Implement the API
            ApiResult<Out> result = handle(apiInput);
            // 按照约定，接口返回值不能为 null，否则被视为接口调用失败。
            if (result == null) {
                result = fail("null of api result");
            }
            return result;
        } catch (Exception e) {

            // When an API exception occurs, scheduled delegates are first used for hosting.
            if (Launcher.ON_API_EXCEPTION_FUNCTION != null) {
                ApiResult<?> result = null;
                try {
                    result = Launcher.ON_API_EXCEPTION_FUNCTION.accept(this, e);
                    return (ApiResult<Out>) result;
                } catch (Exception exception) {
                    e = exception;
                }
            }

            if (e instanceof StatusCodeWithException) {
                StatusCodeWithException e1 = (StatusCodeWithException) e;

                if (e1.getStatusCode() != StatusCode.LOGIN_REQUIRED) {
                    if (e1.getStatusCode() == StatusCode.PARAMETER_VALUE_INVALID) {
                        LOG.warn(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                    } else {
                        LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                    }
                }

                return fail(e1.getStatusCode().getCode(), e1.getMessage());
            } else {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                return fail(e.getMessage());

            }
        } finally {
            apiRunningCount.decrement();
        }
    }

    /**
     * 将入参由 JSON 转换为实体
     */
    private In getInput(String method, JSONObject requestParams, HttpServletRequest request) throws Exception {
        Class<In> apiInputClass = (Class<In>) getInputClass();

        In apiInput = TempSm2Cache.decrypt(requestParams, apiInputClass);
        apiInput.method = method.toUpperCase();
        apiInput.request = request;
        apiInput.rawRequestParams = requestParams;
        // The parameter checking
        apiInput.checkAndStandardize();
        return apiInput;
    }

    /**
     * Check the current concurrency of the API
     */
    private synchronized boolean checkParallelism(String apiClassName) {

        if (!API_PARALLELISM.containsKey(apiClassName)) {
            API_PARALLELISM.put(apiClassName, new LongAdder());
        }

        LongAdder longAdder = API_PARALLELISM.get(apiClassName);

        // 如果允许并发，检查并发量。
        if (canParallel()) {
            return longAdder.longValue() < parallelism();
        }
        // 如果不允许并发，则并发量不能超过1。
        else {
            return longAdder.longValue() < 1;
        }

    }

    /**
     * Maximum parallelism allowed by an interface
     */
    protected int parallelism() {
        return Integer.MAX_VALUE;
    }

    /**
     * Specifies whether the interface allows concurrency. The default value is yes.
     * <p>
     * Override this method in a subclass if changes are needed.
     */
    public boolean canParallel() {
        return true;
    }


    protected ApiResult<?> fail(StatusCodeWithException e, Object data) {
        ApiResult<Object> result = new ApiResult<>();
        result.code = e.getStatusCode().getCode();
        result.message = e.getMessage();
        result.data = data;
        return result;
    }

    protected ApiResult<Out> fail(Exception e) {
        return fail(-1, e.getClass().getSimpleName() + " " + e.getMessage(), null);
    }

    protected ApiResult<Out> fail(String message) {
        return fail(-1, message, null);
    }

    protected ApiResult<Out> fail(StatusCode status) {
        return fail(status.getCode(), status.getMessage(), null);
    }

    public ApiResult<Out> fail(int code, String message) {
        return fail(code, message, null);
    }

    protected ApiResult<Out> fail(int code, String message, Out data) {
        ApiResult<Out> response = new ApiResult<>();
        response.code = code;
        response.message = message;
        response.data = data;
        return response;
    }

    protected ApiResult<ResponseEntity<?>> file(File file) throws StatusCodeWithException {
        if (!file.exists()) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("文件不存在：" + file.getAbsolutePath());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "public, max-age=3600");
        headers.add("Content-Disposition", "attachment; filename=" + file.getName());
        headers.add("Last-Modified", file.lastModified() + "");
        headers.add("ETag", String.valueOf(file.lastModified()));
        headers.add("Access-Control-Expose-Headers", "Content-Disposition");

        ResponseEntity<FileSystemResource> response = ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType(ContentType.of(file)))
                .body(new FileSystemResource(file));

        ApiResult<ResponseEntity<?>> result = new ApiResult<>();
        result.data = response;
        return result;
    }

    protected ApiResult<Out> success(Out data) {
        ApiResult<Out> response = new ApiResult<>();
        response.data = data;
        return response;
    }

    protected ApiResult<Out> success() {
        ApiResult<Out> response = new ApiResult<>();
        response.data = null;
        return response;
    }

    /**
     * Wrap the union API return result as the board API return result.
     */
    protected ApiResult<Object> unionApiResultToBoardApiResult(JSONObject json) {

        ApiResult<Object> result = new ApiResult<>();
        result.code = json.getInteger("code");
        result.message = json.getString("message");
        result.data = json.get("data");

        return result;
    }

    public Class<?> getInputClass() {
        return getInputClass(getClass());
    }

    /**
     * Gets the input parameter type of the current API
     */
    private Class<?> getInputClass(Class<? extends AbstractApi> apiClass) {
        while (!(apiClass.getGenericSuperclass() instanceof ParameterizedType)) {
            apiClass = (Class<? extends AbstractApi>) apiClass.getSuperclass();
        }

        Type[] types = ((ParameterizedType) apiClass.getGenericSuperclass()).getActualTypeArguments();
        if (types.length > 0) {
            try {
                Class<?> type = (Class<?>) types[0];
                if (AbstractApiInput.class.isAssignableFrom(type)) {
                    return type;
                }
            } catch (ClassCastException e) {
                // 当此处发生异常时，通常是因为接口中的泛型参数不是AbstractApiInput的子类。
                // 这里不用做任何处理，下面的代码会尝试从父类中继续寻找。
            }
        }

        return getInputClass((Class<? extends AbstractApi>) apiClass.getSuperclass());
    }

    public Class<?> getOutputClass() {
        return getOutputClass(getClass());
    }

    private Class<?> getOutputClass(Class<? extends AbstractApi> apiClass) {

        // 循环，直到找到泛型参数。
        while (!(apiClass.getGenericSuperclass() instanceof ParameterizedType)) {
            apiClass = (Class<? extends AbstractApi>) apiClass.getSuperclass();
        }

        // 获取泛型类型列表
        Type[] tTypes = ((ParameterizedType) apiClass.getGenericSuperclass()).getActualTypeArguments();

        if (tTypes.length == 2) {
            Type tType = tTypes[1];
            return tType instanceof ParameterizedTypeImpl
                    ? ((ParameterizedTypeImpl) tType).getRawType()
                    : (Class<?>) tType;
        }

        // 如果泛型类型只有一个，可能是IN，也可能是OUT，需要判断。
        if (tTypes.length == 1) {
            Type tType = tTypes[0];
            Class<?> tClass = tType instanceof ParameterizedTypeImpl
                    ? ((ParameterizedTypeImpl) tType).getRawType()
                    : (Class<?>) tType;

            // 如果是 IN，递归继续寻找。
            if (AbstractApiInput.class.isAssignableFrom(tClass)) {
                apiClass = (Class<? extends AbstractApi>) apiClass.getSuperclass();
                return getOutputClass(apiClass);
            } else {
                return tClass;
            }
        }

        return null;
    }
}
