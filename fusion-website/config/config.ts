import { defineConfig } from 'umi';
import routes from './routes';
const base = 'fusion';
//OUTPUT_EXTRA：需要将编译后的文件复制到Java后端项目中 ，Java后端项目以website目录来确认是接口还是地址
const OUTPUT_EXTRA = process.env.OUTPUT_EXTRA||''
export default defineConfig({
    outputPath: `./dist/${base}/${OUTPUT_EXTRA}`,
    publicPath: `/${base}/`,
    hash: true,
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
    },
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
    manifest: {
      basePath: '/',
    },
    fastRefresh: true,
    mfsu:false
  });