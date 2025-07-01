import { getCLS, getFID, getFCP, getLCP, getTTFB } from 'web-vitals';

// Performance monitoring and analytics
class PerformanceMonitor {
  constructor(config = {}) {
    this.config = {
      enableAnalytics: true,
      enableConsoleLog: process.env.NODE_ENV === 'development',
      analyticsEndpoint: '/api/analytics/performance',
      ...config,
    };

    this.metrics = new Map();
    this.initWebVitals();
  }

  // Initialize Web Vitals monitoring
  initWebVitals() {
    const reportMetric = (metric) => {
      this.metrics.set(metric.name, metric);
      this.reportMetric(metric);
    };

    getCLS(reportMetric);
    getFID(reportMetric);
    getFCP(reportMetric);
    getLCP(reportMetric);
    getTTFB(reportMetric);
  }

  // Report metric to analytics service
  reportMetric(metric) {
    if (this.config.enableConsoleLog) {
      console.log(`${metric.name}: ${metric.value}ms (${metric.rating})`);
    }

    if (this.config.enableAnalytics) {
      this.sendToAnalytics(metric);
    }
  }

  // Send metrics to analytics service
  async sendToAnalytics(metric) {
    try {
      await fetch(this.config.analyticsEndpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          name: metric.name,
          value: metric.value,
          rating: metric.rating,
          id: metric.id,
          timestamp: Date.now(),
          url: window.location.href,
          userAgent: navigator.userAgent,
        }),
      });
    } catch (error) {
      console.warn('Failed to send performance metric:', error);
    }
  }

  // Get all collected metrics
  getMetrics() {
    return Object.fromEntries(this.metrics);
  }

  // Mark custom performance timing
  mark(name) {
    performance.mark(name);
  }

  // Measure performance between marks
  measure(name, startMark, endMark) {
    performance.measure(name, startMark, endMark);
    const entries = performance.getEntriesByName(name);
    const duration = entries[entries.length - 1].duration;
    
    this.reportMetric({
      name: `custom.${name}`,
      value: duration,
      rating: duration < 100 ? 'good' : duration < 300 ? 'needs-improvement' : 'poor',
    });

    return duration;
  }
}

// Resource loading performance utilities
export const ResourceMonitor = {
  // Monitor image loading performance
  monitorImageLoad(img, name) {
    const startTime = performance.now();
    
    return new Promise((resolve, reject) => {
      img.onload = () => {
        const duration = performance.now() - startTime;
        console.log(`Image ${name} loaded in ${duration.toFixed(2)}ms`);
        resolve(duration);
      };
      
      img.onerror = reject;
    });
  },

  // Monitor script loading performance
  monitorScriptLoad(src, name) {
    const startTime = performance.now();
    const script = document.createElement('script');
    
    return new Promise((resolve, reject) => {
      script.onload = () => {
        const duration = performance.now() - startTime;
        console.log(`Script ${name} loaded in ${duration.toFixed(2)}ms`);
        resolve(duration);
      };
      
      script.onerror = reject;
      script.src = src;
      document.head.appendChild(script);
    });
  },

  // Get resource timing data
  getResourceTimings() {
    return performance.getEntriesByType('resource').map(entry => ({
      name: entry.name,
      duration: entry.duration,
      transferSize: entry.transferSize,
      type: entry.initiatorType,
    }));
  },
};

// Performance optimization utilities
export const PerformanceUtils = {
  // Debounce function for performance
  debounce(func, wait, immediate = false) {
    let timeout;
    return function executedFunction(...args) {
      const later = () => {
        timeout = null;
        if (!immediate) func(...args);
      };
      const callNow = immediate && !timeout;
      clearTimeout(timeout);
      timeout = setTimeout(later, wait);
      if (callNow) func(...args);
    };
  },

  // Throttle function for performance
  throttle(func, limit) {
    let inThrottle;
    return function executedFunction(...args) {
      if (!inThrottle) {
        func.apply(this, args);
        inThrottle = true;
        setTimeout(() => inThrottle = false, limit);
      }
    };
  },

  // Request Animation Frame throttle
  rafThrottle(func) {
    let rafId;
    return function executedFunction(...args) {
      if (rafId) return;
      rafId = requestAnimationFrame(() => {
        func.apply(this, args);
        rafId = null;
      });
    };
  },

  // Intersection Observer for lazy loading
  createLazyLoader(callback, options = {}) {
    const defaultOptions = {
      rootMargin: '50px 0px',
      threshold: 0.01,
      ...options,
    };

    return new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          callback(entry.target);
        }
      });
    }, defaultOptions);
  },

  // Memory usage monitoring
  getMemoryUsage() {
    if (performance.memory) {
      return {
        used: Math.round(performance.memory.usedJSHeapSize / 1048576), // MB
        total: Math.round(performance.memory.totalJSHeapSize / 1048576), // MB
        limit: Math.round(performance.memory.jsHeapSizeLimit / 1048576), // MB
      };
    }
    return null;
  },

  // Connection monitoring
  getConnectionInfo() {
    if (navigator.connection) {
      return {
        effectiveType: navigator.connection.effectiveType,
        downlink: navigator.connection.downlink,
        rtt: navigator.connection.rtt,
        saveData: navigator.connection.saveData,
      };
    }
    return null;
  },
};

// Export performance monitor instance
export const performanceMonitor = new PerformanceMonitor();

// Auto-initialize performance monitoring
if (typeof window !== 'undefined') {
  // Monitor page load performance
  window.addEventListener('load', () => {
    performanceMonitor.mark('pageLoad');
    
    // Report loading performance
    setTimeout(() => {
      const navigation = performance.getEntriesByType('navigation')[0];
      if (navigation) {
        console.log('Page Load Performance:', {
          domContentLoaded: navigation.domContentLoadedEventEnd - navigation.navigationStart,
          loadComplete: navigation.loadEventEnd - navigation.navigationStart,
          firstByte: navigation.responseStart - navigation.navigationStart,
        });
      }
    }, 0);
  });

  // Monitor unload performance
  window.addEventListener('beforeunload', () => {
    // Send beacon with performance data
    const metrics = performanceMonitor.getMetrics();
    if (navigator.sendBeacon && Object.keys(metrics).length > 0) {
      navigator.sendBeacon('/api/analytics/performance-beacon', JSON.stringify(metrics));
    }
  });
}

export default performanceMonitor;