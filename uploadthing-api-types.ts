// UploadThing API TypeScript Interfaces
// Copy this file to your frontend project for type safety

export interface UploadThingResponse {
  success: boolean;
  message: string;
  deletedFiles: string[];
  failedFiles: string[];
}

export interface UploadThingError {
  success: false;
  message: string;
  deletedFiles: [];
  failedFiles: [];
}

export interface UploadThingSuccess {
  success: true;
  message: string;
  deletedFiles: string[];
  failedFiles: string[];
}

// API Endpoint URLs
export const UPLOADTHING_API_ENDPOINTS = {
  HEALTH: '/api/v1/uploadthing/health',
  DELETE_SINGLE_FILE: '/api/v1/uploadthing/files',
  DELETE_MULTIPLE_FILES: '/api/v1/uploadthing/files',
  DELETE_SINGLE_FILE_BY_URL: '/api/v1/uploadthing/files/url',
  DELETE_MULTIPLE_FILES_BY_URLS: '/api/v1/uploadthing/files/urls',
  DELETE_FILES_ASYNC: '/api/v1/uploadthing/files/async',
  DELETE_FILES_BY_URLS_ASYNC: '/api/v1/uploadthing/files/urls/async',
} as const;

// Request types
export type FileKey = string;
export type UploadThingUrl = string;
export type FileKeyList = FileKey[];
export type UploadThingUrlList = UploadThingUrl[];

// API Request interfaces
export interface DeleteSingleFileRequest {
  fileKey: FileKey;
}

export interface DeleteMultipleFilesRequest {
  fileKeys: FileKeyList;
}

export interface DeleteSingleFileByUrlRequest {
  url: UploadThingUrl;
}

export interface DeleteMultipleFilesByUrlsRequest {
  urls: UploadThingUrlList;
}

// HTTP Status codes
export enum UploadThingHttpStatus {
  OK = 200,
  ACCEPTED = 202,
  BAD_REQUEST = 400,
  UNAUTHORIZED = 401,
  FORBIDDEN = 403,
  NOT_FOUND = 404,
  INTERNAL_SERVER_ERROR = 500,
  SERVICE_UNAVAILABLE = 503,
}

// Error types
export enum UploadThingErrorType {
  VALIDATION_ERROR = 'VALIDATION_ERROR',
  NETWORK_ERROR = 'NETWORK_ERROR',
  AUTHENTICATION_ERROR = 'AUTHENTICATION_ERROR',
  SERVER_ERROR = 'SERVER_ERROR',
  UNKNOWN_ERROR = 'UNKNOWN_ERROR',
}

export interface UploadThingApiError {
  type: UploadThingErrorType;
  message: string;
  statusCode: number;
  originalError?: any;
}

// Utility types
export type UploadThingApiResponse<T = UploadThingResponse> = Promise<T>;
export type UploadThingApiErrorResponse = Promise<UploadThingApiError>;

// Configuration interface
export interface UploadThingConfig {
  baseUrl: string;
  timeout: number;
  retryAttempts: number;
  retryDelay: number;
}

// Hook/Service state interface
export interface UploadThingState {
  loading: boolean;
  error: UploadThingApiError | null;
  lastResponse: UploadThingResponse | null;
}

// React Hook return type
export interface UseUploadThingReturn {
  // State
  loading: boolean;
  error: UploadThingApiError | null;
  lastResponse: UploadThingResponse | null;
  
  // Actions
  deleteFileByUrl: (url: UploadThingUrl) => UploadThingApiResponse;
  deleteMultipleFilesByUrls: (urls: UploadThingUrlList) => UploadThingApiResponse;
  deleteFileByKey: (fileKey: FileKey) => UploadThingApiResponse;
  deleteMultipleFilesByKeys: (fileKeys: FileKeyList) => UploadThingApiResponse;
  deleteFilesAsync: (fileKeys: FileKeyList) => UploadThingApiResponse;
  deleteFilesByUrlsAsync: (urls: UploadThingUrlList) => UploadThingApiResponse;
  checkHealth: () => Promise<string>;
  
  // Utilities
  clearError: () => void;
  reset: () => void;
}

// URL validation utilities
export interface UploadThingUrlValidator {
  isValidUrl: (url: string) => boolean;
  extractFileKey: (url: UploadThingUrl) => FileKey;
  buildUrl: (fileKey: FileKey) => UploadThingUrl;
}

// Example usage with React
export interface UploadThingComponentProps {
  onFileDeleted?: (response: UploadThingSuccess) => void;
  onError?: (error: UploadThingApiError) => void;
  onLoadingChange?: (loading: boolean) => void;
}

// Example usage with Vue
export interface UploadThingComposable {
  loading: Ref<boolean>;
  error: Ref<UploadThingApiError | null>;
  lastResponse: Ref<UploadThingResponse | null>;
  
  deleteFileByUrl: (url: UploadThingUrl) => Promise<UploadThingResponse>;
  deleteMultipleFilesByUrls: (urls: UploadThingUrlList) => Promise<UploadThingResponse>;
  clearError: () => void;
  reset: () => void;
}

// Example usage with Angular
export interface UploadThingServiceInterface {
  deleteFileByUrl(url: UploadThingUrl): Observable<UploadThingResponse>;
  deleteMultipleFilesByUrls(urls: UploadThingUrlList): Observable<UploadThingResponse>;
  deleteFileByKey(fileKey: FileKey): Observable<UploadThingResponse>;
  deleteMultipleFilesByKeys(fileKeys: FileKeyList): Observable<UploadThingResponse>;
  checkHealth(): Observable<string>;
}

// Constants
export const UPLOADTHING_CONSTANTS = {
  DOMAIN: 'utfs.io',
  PATH_PREFIX: '/f/',
  MAX_FILES_PER_REQUEST: 100,
  DEFAULT_TIMEOUT: 30000,
  DEFAULT_RETRY_ATTEMPTS: 3,
  DEFAULT_RETRY_DELAY: 1000,
} as const;

// Type guards
export const isUploadThingUrl = (url: string): url is UploadThingUrl => {
  try {
    const urlObj = new URL(url);
    return urlObj.hostname === UPLOADTHING_CONSTANTS.DOMAIN && 
           urlObj.pathname.startsWith(UPLOADTHING_CONSTANTS.PATH_PREFIX);
  } catch {
    return false;
  }
};

export const isUploadThingSuccess = (response: UploadThingResponse): response is UploadThingSuccess => {
  return response.success === true;
};

export const isUploadThingError = (response: UploadThingResponse): response is UploadThingError => {
  return response.success === false;
};

// Utility functions
export const extractFileKeyFromUrl = (url: UploadThingUrl): FileKey => {
  if (!isUploadThingUrl(url)) {
    throw new Error(`Invalid UploadThing URL: ${url}`);
  }
  
  const urlObj = new URL(url);
  return urlObj.pathname.substring(UPLOADTHING_CONSTANTS.PATH_PREFIX.length);
};

export const buildUploadThingUrl = (fileKey: FileKey): UploadThingUrl => {
  return `https://${UPLOADTHING_CONSTANTS.DOMAIN}${UPLOADTHING_CONSTANTS.PATH_PREFIX}${fileKey}`;
};

export const validateFileKeyList = (fileKeys: FileKeyList): void => {
  if (!Array.isArray(fileKeys) || fileKeys.length === 0) {
    throw new Error('File keys list cannot be empty');
  }
  
  if (fileKeys.length > UPLOADTHING_CONSTANTS.MAX_FILES_PER_REQUEST) {
    throw new Error(`Cannot delete more than ${UPLOADTHING_CONSTANTS.MAX_FILES_PER_REQUEST} files at once`);
  }
  
  if (fileKeys.some(key => !key || typeof key !== 'string')) {
    throw new Error('All file keys must be non-empty strings');
  }
};

export const validateUrlList = (urls: UploadThingUrlList): void => {
  if (!Array.isArray(urls) || urls.length === 0) {
    throw new Error('URLs list cannot be empty');
  }
  
  if (urls.length > UPLOADTHING_CONSTANTS.MAX_FILES_PER_REQUEST) {
    throw new Error(`Cannot delete more than ${UPLOADTHING_CONSTANTS.MAX_FILES_PER_REQUEST} files at once`);
  }
  
  if (urls.some(url => !isUploadThingUrl(url))) {
    throw new Error('All URLs must be valid UploadThing URLs');
  }
};

// Error message constants
export const UPLOADTHING_ERROR_MESSAGES = {
  EMPTY_FILE_KEYS: 'File keys list cannot be empty',
  EMPTY_URLS: 'URLs list cannot be empty',
  INVALID_URL: 'Invalid UploadThing URL',
  NETWORK_ERROR: 'Network error occurred',
  TIMEOUT_ERROR: 'Request timeout',
  SERVER_ERROR: 'Server error occurred',
  AUTHENTICATION_ERROR: 'Authentication required',
  TOO_MANY_FILES: `Cannot delete more than ${UPLOADTHING_CONSTANTS.MAX_FILES_PER_REQUEST} files at once`,
} as const;
