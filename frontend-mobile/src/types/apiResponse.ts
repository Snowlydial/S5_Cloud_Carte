// src/types/apiResponse.ts
export interface ApiResponse<T = any> {
  success: boolean;
  code: number;
  message: string;
  data?: T;
  error?: {
    type: string;
    details?: string;
  }
}
