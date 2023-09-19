import { defineConfig } from 'umi';
import routes from './routes';

const base = 'fusion';
export default defineConfig({
    base,
    outputPath: `./dist/${base}/`,
    publicPath: `/${base}/`,
    hash: true,
    define: {
      'process.env.HOST_ENV': process.env.HOST_ENV,
    },
    lessLoader: {
      modifyVars: {
        '@ant-prefix': base,
        '@biz-prefix': base,
        '@pro-prefix': base,
      },
      javascriptEnabled: true,
    },
    mountElementId: 'root-slave',
    // {
    //   // https://umijs.org/zh-CN/plugins/plugin-layout
    //   locale: true,
    //   siderWidth: 208,
    //   ...defaultSettings,
    // },
    // https://umijs.org/zh-CN/plugins/plugin-locale
    targets: {
      ie: 11,
    },
    // umi routes: https://umijs.org/docs/routing
    routes,
    // Theme for antd: https://ant.design/docs/react/customize-theme-cn
    theme: {
      'primary-color': '#1890ff',
    },
    title: false,
    ignoreMomentLocale: true,
    manifest: {
      basePath: '/',
    },
    fastRefresh: true,
  });