module.exports = {
  '/api': {
    target: process.env.BACKEND_URL ?? 'http://localhost:8081',
    secure: false,
    changeOrigin: true,
  }
};
