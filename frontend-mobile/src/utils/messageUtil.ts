import type { ApiResponse } from "@/types/apiResponse";



export function getErrorMessage(response: ApiResponse): string {
  if (!response.error) return response.message;
  return response.error.details || response.message;
}

export function getSuccessMessage(response: ApiResponse): string {
  return response.message;
}