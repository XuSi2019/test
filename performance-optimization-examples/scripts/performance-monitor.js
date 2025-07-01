#!/usr/bin/env node

import fs from 'fs';
import path from 'path';
import { execSync } from 'child_process';

/**
 * Performance monitoring and alerting script
 * Analyzes bundle size, lighthouse scores, and performance metrics
 */

const PERFORMANCE_BUDGETS = {
  // Bundle sizes (gzipped)
  totalBundleSize: 500 * 1024, // 500KB
  jsChunkSize: 250 * 1024,     // 250KB per chunk
  cssSize: 50 * 1024,          // 50KB total CSS
  imageSize: 1 * 1024 * 1024,  // 1MB per image

  // Lighthouse scores (0-100)
  performance: 90,
  accessibility: 95,
  bestPractices: 90,
  seo: 95,

  // Core Web Vitals
  firstContentfulPaint: 1800,     // ms
  largestContentfulPaint: 2500,   // ms
  firstInputDelay: 100,           // ms
  cumulativeLayoutShift: 0.1,     // score
  totalBlockingTime: 300,         // ms
};

class PerformanceMonitor {
  constructor() {
    this.results = {
      bundleAnalysis: null,
      lighthouseScores: null,
      webVitals: null,
      violations: [],
      recommendations: [],
    };
  }

  // Analyze bundle size
  async analyzeBundleSize() {
    console.log('📊 Analyzing bundle size...');

    try {
      // Get build output
      const distPath = path.join(process.cwd(), 'dist');
      const stats = this.getDirectoryStats(distPath);

      this.results.bundleAnalysis = {
        totalSize: stats.totalSize,
        jsSize: stats.jsSize,
        cssSize: stats.cssSize,
        imageSize: stats.imageSize,
        files: stats.files,
      };

      // Check against budgets
      this.checkBundleBudgets();

      console.log(`✅ Bundle analysis complete`);
      console.log(`   Total size: ${this.formatBytes(stats.totalSize)}`);
      console.log(`   JS size: ${this.formatBytes(stats.jsSize)}`);
      console.log(`   CSS size: ${this.formatBytes(stats.cssSize)}`);

    } catch (error) {
      console.error('❌ Bundle analysis failed:', error.message);
      throw error;
    }
  }

  // Get directory statistics
  getDirectoryStats(dirPath) {
    const stats = {
      totalSize: 0,
      jsSize: 0,
      cssSize: 0,
      imageSize: 0,
      files: [],
    };

    const readDir = (dir) => {
      const files = fs.readdirSync(dir);

      files.forEach(file => {
        const filePath = path.join(dir, file);
        const stat = fs.statSync(filePath);

        if (stat.isDirectory()) {
          readDir(filePath);
        } else {
          const ext = path.extname(file).toLowerCase();
          const size = stat.size;

          stats.totalSize += size;
          stats.files.push({
            path: path.relative(process.cwd(), filePath),
            size,
            ext,
          });

          // Categorize by file type
          if (['.js', '.mjs'].includes(ext)) {
            stats.jsSize += size;
          } else if (['.css'].includes(ext)) {
            stats.cssSize += size;
          } else if (['.png', '.jpg', '.jpeg', '.gif', '.svg', '.webp', '.avif'].includes(ext)) {
            stats.imageSize += size;
          }
        }
      });
    };

    readDir(dirPath);
    return stats;
  }

  // Check bundle against budgets
  checkBundleBudgets() {
    const { bundleAnalysis } = this.results;

    if (bundleAnalysis.totalSize > PERFORMANCE_BUDGETS.totalBundleSize) {
      this.results.violations.push({
        type: 'bundle-size',
        severity: 'error',
        message: `Total bundle size (${this.formatBytes(bundleAnalysis.totalSize)}) exceeds budget (${this.formatBytes(PERFORMANCE_BUDGETS.totalBundleSize)})`,
        actual: bundleAnalysis.totalSize,
        expected: PERFORMANCE_BUDGETS.totalBundleSize,
      });
    }

    if (bundleAnalysis.jsSize > PERFORMANCE_BUDGETS.jsChunkSize) {
      this.results.violations.push({
        type: 'js-size',
        severity: 'warning',
        message: `JavaScript size (${this.formatBytes(bundleAnalysis.jsSize)}) exceeds budget (${this.formatBytes(PERFORMANCE_BUDGETS.jsChunkSize)})`,
        actual: bundleAnalysis.jsSize,
        expected: PERFORMANCE_BUDGETS.jsChunkSize,
      });
    }

    if (bundleAnalysis.cssSize > PERFORMANCE_BUDGETS.cssSize) {
      this.results.violations.push({
        type: 'css-size',
        severity: 'warning',
        message: `CSS size (${this.formatBytes(bundleAnalysis.cssSize)}) exceeds budget (${this.formatBytes(PERFORMANCE_BUDGETS.cssSize)})`,
        actual: bundleAnalysis.cssSize,
        expected: PERFORMANCE_BUDGETS.cssSize,
      });
    }
  }

  // Analyze Lighthouse results
  async analyzeLighthouse() {
    console.log('🏮 Analyzing Lighthouse results...');

    try {
      const lhciPath = path.join(process.cwd(), '.lighthouseci');
      const manifestPath = path.join(lhciPath, 'manifest.json');

      if (!fs.existsSync(manifestPath)) {
        throw new Error('Lighthouse CI results not found. Run lighthouse first.');
      }

      const manifest = JSON.parse(fs.readFileSync(manifestPath, 'utf8'));
      const latestRun = manifest[0]; // Get latest run

      if (!latestRun) {
        throw new Error('No lighthouse runs found in manifest');
      }

      const reportPath = path.join(lhciPath, latestRun.jsonPath);
      const report = JSON.parse(fs.readFileSync(reportPath, 'utf8'));

      this.results.lighthouseScores = {
        performance: report.categories.performance.score * 100,
        accessibility: report.categories.accessibility.score * 100,
        bestPractices: report.categories['best-practices'].score * 100,
        seo: report.categories.seo.score * 100,
        audits: this.extractKeyAudits(report.audits),
      };

      // Check against budgets
      this.checkLighthouseBudgets();

      console.log('✅ Lighthouse analysis complete');
      console.log(`   Performance: ${this.results.lighthouseScores.performance}`);
      console.log(`   Accessibility: ${this.results.lighthouseScores.accessibility}`);

    } catch (error) {
      console.error('❌ Lighthouse analysis failed:', error.message);
      throw error;
    }
  }

  // Extract key audits from Lighthouse report
  extractKeyAudits(audits) {
    const keyAudits = [
      'first-contentful-paint',
      'largest-contentful-paint',
      'first-input-delay',
      'cumulative-layout-shift',
      'total-blocking-time',
      'speed-index',
      'interactive',
    ];

    return keyAudits.reduce((extracted, auditKey) => {
      if (audits[auditKey]) {
        extracted[auditKey] = {
          score: audits[auditKey].score,
          numericValue: audits[auditKey].numericValue,
          displayValue: audits[auditKey].displayValue,
        };
      }
      return extracted;
    }, {});
  }

  // Check Lighthouse scores against budgets
  checkLighthouseBudgets() {
    const { lighthouseScores } = this.results;

    // Check category scores
    Object.entries(PERFORMANCE_BUDGETS).forEach(([key, budget]) => {
      if (key in lighthouseScores && lighthouseScores[key] < budget) {
        this.results.violations.push({
          type: 'lighthouse-score',
          severity: 'error',
          message: `${key} score (${lighthouseScores[key]}) below budget (${budget})`,
          actual: lighthouseScores[key],
          expected: budget,
        });
      }
    });

    // Check Core Web Vitals
    const { audits } = lighthouseScores;
    
    if (audits['first-contentful-paint']?.numericValue > PERFORMANCE_BUDGETS.firstContentfulPaint) {
      this.results.violations.push({
        type: 'core-web-vital',
        severity: 'error',
        message: `First Contentful Paint (${audits['first-contentful-paint'].numericValue}ms) exceeds budget (${PERFORMANCE_BUDGETS.firstContentfulPaint}ms)`,
        actual: audits['first-contentful-paint'].numericValue,
        expected: PERFORMANCE_BUDGETS.firstContentfulPaint,
      });
    }

    if (audits['largest-contentful-paint']?.numericValue > PERFORMANCE_BUDGETS.largestContentfulPaint) {
      this.results.violations.push({
        type: 'core-web-vital',
        severity: 'error',
        message: `Largest Contentful Paint (${audits['largest-contentful-paint'].numericValue}ms) exceeds budget (${PERFORMANCE_BUDGETS.largestContentfulPaint}ms)`,
        actual: audits['largest-contentful-paint'].numericValue,
        expected: PERFORMANCE_BUDGETS.largestContentfulPaint,
      });
    }
  }

  // Generate recommendations
  generateRecommendations() {
    console.log('💡 Generating performance recommendations...');

    const { bundleAnalysis, lighthouseScores } = this.results;

    // Bundle size recommendations
    if (bundleAnalysis.totalSize > PERFORMANCE_BUDGETS.totalBundleSize * 0.8) {
      this.results.recommendations.push({
        type: 'bundle-optimization',
        priority: 'high',
        title: 'Optimize bundle size',
        description: 'Consider implementing code splitting, tree shaking, or removing unused dependencies',
        impact: 'High - Reduces load time and improves user experience',
      });
    }

    // Performance score recommendations
    if (lighthouseScores.performance < 90) {
      this.results.recommendations.push({
        type: 'performance-optimization',
        priority: 'high',
        title: 'Improve performance score',
        description: 'Focus on Core Web Vitals, optimize images, and reduce JavaScript execution time',
        impact: 'High - Better user experience and SEO rankings',
      });
    }

    // Image optimization
    if (bundleAnalysis.imageSize > PERFORMANCE_BUDGETS.imageSize) {
      this.results.recommendations.push({
        type: 'image-optimization',
        priority: 'medium',
        title: 'Optimize images',
        description: 'Use modern image formats (WebP, AVIF), implement responsive images, and compress images',
        impact: 'Medium - Faster loading and reduced bandwidth usage',
      });
    }

    console.log(`✅ Generated ${this.results.recommendations.length} recommendations`);
  }

  // Generate detailed report
  generateReport() {
    const timestamp = new Date().toISOString();
    const report = {
      timestamp,
      summary: {
        totalViolations: this.results.violations.length,
        errorViolations: this.results.violations.filter(v => v.severity === 'error').length,
        warningViolations: this.results.violations.filter(v => v.severity === 'warning').length,
        totalRecommendations: this.results.recommendations.length,
      },
      results: this.results,
      budgets: PERFORMANCE_BUDGETS,
    };

    // Save report
    const reportPath = path.join(process.cwd(), 'performance-report.json');
    fs.writeFileSync(reportPath, JSON.stringify(report, null, 2));

    // Generate markdown report
    this.generateMarkdownReport(report);

    console.log(`📊 Performance report saved to: ${reportPath}`);
    return report;
  }

  // Generate markdown report for GitHub comments
  generateMarkdownReport(report) {
    const { bundleAnalysis, lighthouseScores } = this.results;
    
    let markdown = '# 📊 Performance Report\n\n';
    
    // Summary
    markdown += '## Summary\n\n';
    markdown += `- **Total Violations**: ${report.summary.totalViolations} (${report.summary.errorViolations} errors, ${report.summary.warningViolations} warnings)\n`;
    markdown += `- **Recommendations**: ${report.summary.totalRecommendations}\n`;
    markdown += `- **Generated**: ${new Date(report.timestamp).toLocaleString()}\n\n`;

    // Bundle Analysis
    if (bundleAnalysis) {
      markdown += '## 📦 Bundle Analysis\n\n';
      markdown += '| Metric | Size | Budget | Status |\n';
      markdown += '|--------|------|--------|--------|\n';
      markdown += `| Total Bundle | ${this.formatBytes(bundleAnalysis.totalSize)} | ${this.formatBytes(PERFORMANCE_BUDGETS.totalBundleSize)} | ${bundleAnalysis.totalSize <= PERFORMANCE_BUDGETS.totalBundleSize ? '✅' : '❌'} |\n`;
      markdown += `| JavaScript | ${this.formatBytes(bundleAnalysis.jsSize)} | ${this.formatBytes(PERFORMANCE_BUDGETS.jsChunkSize)} | ${bundleAnalysis.jsSize <= PERFORMANCE_BUDGETS.jsChunkSize ? '✅' : '❌'} |\n`;
      markdown += `| CSS | ${this.formatBytes(bundleAnalysis.cssSize)} | ${this.formatBytes(PERFORMANCE_BUDGETS.cssSize)} | ${bundleAnalysis.cssSize <= PERFORMANCE_BUDGETS.cssSize ? '✅' : '❌'} |\n\n`;
    }

    // Lighthouse Scores
    if (lighthouseScores) {
      markdown += '## 🏮 Lighthouse Scores\n\n';
      markdown += '| Category | Score | Budget | Status |\n';
      markdown += '|----------|-------|--------|--------|\n';
      markdown += `| Performance | ${lighthouseScores.performance} | ${PERFORMANCE_BUDGETS.performance} | ${lighthouseScores.performance >= PERFORMANCE_BUDGETS.performance ? '✅' : '❌'} |\n`;
      markdown += `| Accessibility | ${lighthouseScores.accessibility} | ${PERFORMANCE_BUDGETS.accessibility} | ${lighthouseScores.accessibility >= PERFORMANCE_BUDGETS.accessibility ? '✅' : '❌'} |\n`;
      markdown += `| Best Practices | ${lighthouseScores.bestPractices} | ${PERFORMANCE_BUDGETS.bestPractices} | ${lighthouseScores.bestPractices >= PERFORMANCE_BUDGETS.bestPractices ? '✅' : '❌'} |\n`;
      markdown += `| SEO | ${lighthouseScores.seo} | ${PERFORMANCE_BUDGETS.seo} | ${lighthouseScores.seo >= PERFORMANCE_BUDGETS.seo ? '✅' : '❌'} |\n\n`;
    }

    // Violations
    if (this.results.violations.length > 0) {
      markdown += '## ⚠️ Performance Violations\n\n';
      this.results.violations.forEach(violation => {
        const icon = violation.severity === 'error' ? '❌' : '⚠️';
        markdown += `${icon} **${violation.type}**: ${violation.message}\n\n`;
      });
    }

    // Recommendations
    if (this.results.recommendations.length > 0) {
      markdown += '## 💡 Recommendations\n\n';
      this.results.recommendations.forEach(rec => {
        const priority = rec.priority === 'high' ? '🔴' : rec.priority === 'medium' ? '🟡' : '🟢';
        markdown += `${priority} **${rec.title}** (${rec.priority} priority)\n`;
        markdown += `${rec.description}\n`;
        markdown += `*Impact: ${rec.impact}*\n\n`;
      });
    }

    fs.writeFileSync('performance-report.md', markdown);
  }

  // Format bytes to human readable
  formatBytes(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  // Run complete analysis
  async run() {
    console.log('🚀 Starting performance analysis...\n');

    try {
      await this.analyzeBundleSize();
      await this.analyzeLighthouse();
      this.generateRecommendations();
      const report = this.generateReport();

      console.log('\n📋 Performance Analysis Summary:');
      console.log(`   Violations: ${report.summary.totalViolations}`);
      console.log(`   Recommendations: ${report.summary.totalRecommendations}`);

      // Exit with error code if there are critical violations
      const criticalViolations = this.results.violations.filter(v => v.severity === 'error');
      if (criticalViolations.length > 0) {
        console.log('\n❌ Performance analysis failed due to critical violations');
        process.exit(1);
      }

      console.log('\n✅ Performance analysis completed successfully');

    } catch (error) {
      console.error('\n❌ Performance analysis failed:', error.message);
      process.exit(1);
    }
  }
}

// Run if called directly
if (import.meta.url === `file://${process.argv[1]}`) {
  const monitor = new PerformanceMonitor();
  monitor.run();
}

export default PerformanceMonitor;