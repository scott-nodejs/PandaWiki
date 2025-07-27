import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  distDir: 'dist',
  reactStrictMode: false,
  allowedDevOrigins: ['10.10.18.71'],
  output: 'standalone',
  logging: {
    fetches: {
      fullUrl: true,
    },
  },
  async rewrites() {
    const rewritesPath = [];
    if (process.env.NODE_ENV === 'development') {
      rewritesPath.push(
        ...[
          {
            source: '/static-file/:path*',
            destination: `${process.env.NEXT_PUBLIC_API_URL}/static-file/:path*`,
            basePath: false as const,
          },
          // 代理所有API请求到后端
          {
            source: '/client/:path*',
            destination: `${process.env.NEXT_PUBLIC_API_URL}/client/:path*`,
            basePath: false as const,
          },
          {
            source: '/share/:path*',
            destination: `${process.env.NEXT_PUBLIC_API_URL}/share/:path*`,
            basePath: false as const,
          },
          {
            source: '/api/:path*',
            destination: `${process.env.NEXT_PUBLIC_API_URL}/api/:path*`,
            basePath: false as const,
          }
        ]
      );
    }
    return rewritesPath;
  },
};

export default nextConfig;
