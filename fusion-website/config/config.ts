import { defineConfig } from '@umijs/max';
import routes from './routes';
const base = 'fusion';
//OUTPUT_EXTRA：需要将编译后的文件复制到Java后端项目中 ，Java后端项目以website目录来确认是接口还是地址
const OUTPUT_EXTRA = process.env.OUTPUT_EXTRA||''
const BASE_PATH = process.env.BASE_PATH?`${process.env.BASE_PATH}`:`/${base}/`
export default defineConfig({
    base: `${BASE_PATH}`,
    outputPath: `./dist/${OUTPUT_EXTRA}`,
    publicPath:`${BASE_PATH}`,
    extraBabelPlugins:[
      [
        'babel-plugin-import',
        {
          libraryName: '@tianmiantech/pro',
          libraryDirectory: 'src',
          camel2DashComponentName: false,
          style: true
        }
      ]
    ],
    define: {
      'process.env.HOST_ENV': process.env.HOST_ENV,
      'process.env.BASE_PATH': BASE_PATH,
    },
    model: {},
    initialState: {},
    antd: {
      // configProvider
      configProvider: {
        prefixCls:'fusion'
      },
    },
    lessLoader: {
      modifyVars: {
        '@ant-prefix': 'fusion',
        '@biz-prefix': 'fusion',
        '@pro-prefix': 'fusion',
      },
      javascriptEnabled: true,
    },
    locale: {},
    hash:true,
    mountElementId: 'root-slave',
    // {
    //   // https://umijs.org/zh-CN/plugins/plugin-layout
    //   locale: true,
    //   siderWidth: 208,
    //   ...defaultSettings,
    // },
    // https://umijs.org/zh-CN/plugins/plugin-locale
    targets: { chrome: 67 },
    // umi routes: https://umijs.org/docs/routing
    routes,
    // Theme for antd: https://ant.design/docs/react/customize-theme-cn
    title: false,
    ignoreMomentLocale: true,
    fastRefresh: true,
    mfsu:false
  });