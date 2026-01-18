// src/utils/getSuccessMessage.ts
import type { ApiResponse } from "@/types/apiResponse";

export function getSuccessMessage(response: ApiResponse): string {
  return response.message;
}
