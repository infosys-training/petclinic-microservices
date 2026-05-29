import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "RFP Copilot Platform",
  description: "AI-powered RFP/RFI Response Copilot",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className={inter.className}>
        <div className="min-h-screen bg-gray-50">
          <header className="bg-white border-b border-gray-200">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
              <div className="flex items-center justify-between h-16">
                <div className="flex items-center gap-3">
                  <div className="flex items-center gap-2">
                    <div className="h-8 w-8 bg-blue-600 rounded-lg flex items-center justify-center">
                      <span className="text-white font-bold text-sm">RC</span>
                    </div>
                    <h1 className="text-xl font-bold text-gray-900">
                      RFP Copilot
                    </h1>
                  </div>
                  <span className="text-xs bg-blue-100 text-blue-800 px-2 py-0.5 rounded-full font-medium">
                    AI-Powered
                  </span>
                </div>
                <nav className="flex items-center gap-4">
                  <a
                    href="/"
                    className="text-sm text-gray-600 hover:text-gray-900"
                  >
                    Projects
                  </a>
                  <a
                    href="/api/docs"
                    target="_blank"
                    className="text-sm text-gray-600 hover:text-gray-900"
                  >
                    API Docs
                  </a>
                </nav>
              </div>
            </div>
          </header>
          <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            {children}
          </main>
        </div>
      </body>
    </html>
  );
}
