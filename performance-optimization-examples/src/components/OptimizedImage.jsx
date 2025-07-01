import { useState, useRef, useEffect } from 'react';
import { PerformanceUtils } from '../utils/performance';

const OptimizedImage = ({
  src,
  alt,
  width,
  height,
  className = '',
  lazy = true,
  placeholder = 'blur',
  quality = 80,
  formats = ['avif', 'webp'],
  sizes = '100vw',
  priority = false,
  onLoad,
  onError,
  ...props
}) => {
  const [isLoaded, setIsLoaded] = useState(false);
  const [isInView, setIsInView] = useState(!lazy);
  const [hasError, setHasError] = useState(false);
  const imgRef = useRef(null);
  const observerRef = useRef(null);

  // Generate responsive image URLs
  const generateSrcSet = (baseSrc, format) => {
    const breakpoints = [320, 640, 768, 1024, 1280, 1536];
    return breakpoints
      .map(bp => `${baseSrc}?w=${bp}&f=${format}&q=${quality} ${bp}w`)
      .join(', ');
  };

  // Generate picture sources for different formats
  const generateSources = () => {
    return formats.map(format => ({
      type: `image/${format}`,
      srcSet: generateSrcSet(src, format),
    }));
  };

  // Lazy loading with Intersection Observer
  useEffect(() => {
    if (!lazy || priority) {
      setIsInView(true);
      return;
    }

    observerRef.current = PerformanceUtils.createLazyLoader(
      (target) => {
        setIsInView(true);
        observerRef.current?.unobserve(target);
      },
      {
        rootMargin: '50px 0px',
        threshold: 0.01,
      }
    );

    if (imgRef.current) {
      observerRef.current.observe(imgRef.current);
    }

    return () => {
      if (observerRef.current && imgRef.current) {
        observerRef.current.unobserve(imgRef.current);
      }
    };
  }, [lazy, priority]);

  // Handle image load
  const handleLoad = (event) => {
    setIsLoaded(true);
    onLoad?.(event);
  };

  // Handle image error
  const handleError = (event) => {
    setHasError(true);
    onError?.(event);
  };

  // Placeholder component
  const PlaceholderComponent = () => {
    if (placeholder === 'blur') {
      return (
        <div
          className={`absolute inset-0 bg-gray-200 animate-pulse ${className}`}
          style={{ width, height }}
        />
      );
    }
    
    if (placeholder === 'empty') {
      return (
        <div
          className={`bg-gray-100 ${className}`}
          style={{ width, height }}
        />
      );
    }

    return null;
  };

  // Error fallback
  if (hasError) {
    return (
      <div
        className={`bg-gray-100 flex items-center justify-center ${className}`}
        style={{ width, height }}
        {...props}
      >
        <span className="text-gray-400 text-sm">Failed to load image</span>
      </div>
    );
  }

  return (
    <div
      ref={imgRef}
      className={`relative overflow-hidden ${className}`}
      style={{ width, height }}
      {...props}
    >
      {!isLoaded && <PlaceholderComponent />}
      
      {isInView && (
        <picture>
          {generateSources().map((source, index) => (
            <source
              key={index}
              type={source.type}
              srcSet={source.srcSet}
              sizes={sizes}
            />
          ))}
          <img
            src={`${src}?w=${width}&q=${quality}`}
            alt={alt}
            width={width}
            height={height}
            loading={priority ? 'eager' : 'lazy'}
            decoding={priority ? 'sync' : 'async'}
            className={`
              transition-opacity duration-300
              ${isLoaded ? 'opacity-100' : 'opacity-0'}
              ${className}
            `}
            onLoad={handleLoad}
            onError={handleError}
            style={{
              width: '100%',
              height: '100%',
              objectFit: 'cover',
            }}
          />
        </picture>
      )}
    </div>
  );
};

export default OptimizedImage;