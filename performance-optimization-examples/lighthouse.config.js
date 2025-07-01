module.exports = {
  ci: {
    collect: {
      // Number of runs to perform
      numberOfRuns: 3,
      // URL patterns to test
      url: [
        'http://localhost:3001', // Home page
        'http://localhost:3001/dashboard', // Dashboard
        'http://localhost:3001/profile', // Profile page
      ],
      // Lighthouse settings
      settings: {
        // Use desktop configuration
        preset: 'desktop',
        // Custom Chrome flags
        chromeFlags: '--no-sandbox --disable-dev-shm-usage',
        // Throttling settings
        throttling: {
          rttMs: 40,
          throughputKbps: 10240,
          cpuSlowdownMultiplier: 1,
          requestLatencyMs: 0,
          downloadThroughputKbps: 0,
          uploadThroughputKbps: 0,
        },
      },
    },
    assert: {
      // Performance assertions
      assertions: {
        // Core Web Vitals
        'categories:performance': ['error', { minScore: 0.9 }],
        'categories:accessibility': ['error', { minScore: 0.9 }],
        'categories:best-practices': ['error', { minScore: 0.9 }],
        'categories:seo': ['error', { minScore: 0.9 }],
        
        // Specific metrics
        'first-contentful-paint': ['error', { maxNumericValue: 2000 }],
        'largest-contentful-paint': ['error', { maxNumericValue: 2500 }],
        'first-meaningful-paint': ['error', { maxNumericValue: 2000 }],
        'speed-index': ['error', { maxNumericValue: 3000 }],
        'interactive': ['error', { maxNumericValue: 3800 }],
        'total-blocking-time': ['error', { maxNumericValue: 300 }],
        'cumulative-layout-shift': ['error', { maxNumericValue: 0.1 }],
        
        // Resource optimization
        'unused-javascript': ['warn', { maxNumericValue: 20000 }],
        'unused-css-rules': ['warn', { maxNumericValue: 20000 }],
        'render-blocking-resources': ['warn', { maxNumericValue: 500 }],
        'unminified-css': ['error', { maxNumericValue: 0 }],
        'unminified-javascript': ['error', { maxNumericValue: 0 }],
        
        // Image optimization
        'modern-image-formats': ['warn', { maxNumericValue: 50000 }],
        'uses-responsive-images': ['warn', { maxNumericValue: 50000 }],
        'efficient-animated-content': ['warn', { maxNumericValue: 50000 }],
        
        // Network optimization
        'uses-text-compression': ['error', { maxNumericValue: 0 }],
        'uses-rel-preconnect': ['warn', { maxNumericValue: 500 }],
        'uses-rel-preload': ['warn', { maxNumericValue: 500 }],
        
        // JavaScript optimization
        'bootup-time': ['warn', { maxNumericValue: 3500 }],
        'mainthread-work-breakdown': ['warn', { maxNumericValue: 4000 }],
        'third-party-summary': ['warn', { maxNumericValue: 500 }],
      },
    },
    upload: {
      // Upload results to Lighthouse CI server (optional)
      target: 'temporary-public-storage',
      // Or use GitHub status checks
      // target: 'github',
      // githubToken: process.env.LHCI_GITHUB_TOKEN,
    },
    server: {
      // Optional: Run local LHCI server
      // port: 9001,
      // storage: {
      //   storageMethod: 'sql',
      //   sqlDialect: 'sqlite',
      //   sqlDatabasePath: './lhci.db',
      // },
    },
    wizard: {
      // Skip wizard in CI
      skip: true,
    },
  },
};