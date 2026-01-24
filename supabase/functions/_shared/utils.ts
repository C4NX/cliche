import {
    createClient,
    SupabaseClient,
    User,
} from "npm:@supabase/supabase-js@2";
import { corsHeaders } from "./cors.ts";
/**
 * Custom HTTP error class to encapsulate status codes.
 */
export class HttpError extends Error {
    status: number;

    constructor(message: string, status: number) {
        super(message);
        this.status = status;
    }
}

/**
 * Wrapper for Deno.serve to handle errors uniformly.
 * @param handler - The request handler function.
 * @returns A Deno.serve instance.
 */
export function serve(handler: (req: Request) => Promise<Response>) {
    return Deno.serve(async (req: Request) => {
        try {
            return await handler(req);
        } catch (error) {
            if (error instanceof HttpError) {
                return createJsonResponse(
                    { error: error.message },
                    error.status,
                );
            } else if (error instanceof Error) {
                return createJsonResponse(
                    { error: error.message },
                    500,
                );
            } else {
                return createJsonResponse(
                    { error: "Internal Server Error" },
                    500,
                );
            }
        }
    });
}

export function createAnonSupabaseClient(): SupabaseClient {
    const SUPABASE_URL = Deno.env.get("SUPABASE_URL");
    const SUPABASE_ANON_KEY = Deno.env.get("SUPABASE_ANON_KEY");

    if (!SUPABASE_URL || !SUPABASE_ANON_KEY) {
        throw new Error("Supabase environment variables are not set");
    }

    return createClient(SUPABASE_URL, SUPABASE_ANON_KEY);
}

export function createAdminSupabaseClient() {
    const SUPABASE_URL = Deno.env.get("SUPABASE_URL");
    const SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");

    if (!SUPABASE_URL || !SERVICE_ROLE_KEY) {
        throw new Error(
            "Missing SUPABASE_URL or SUPABASE_SERVICE_ROLE_KEY environment variables",
        );
    }

    return createClient(SUPABASE_URL, SERVICE_ROLE_KEY);
}

/**
 * Sanitizes a file name by removing unsafe characters.
 * @param name - The original file name.
 * @returns Sanitized file name.
 */
export function sanitizeFileName(name: string): string {
    // Keep only safe characters; replace others with underscore
    return name
        .trim()
        .replace(/[^a-zA-Z0-9._\-]/g, "_")
        .replace(/_{2,}/g, "_"); // avoid multiple underscores
}

/**
 * Generates a unique file path for storage.
 * @param bucket - The storage bucket name.
 * @param userId - ID of the user uploading the file.
 * @param originalName - Original file name.
 * @param prefix - Optional prefix for the file path.
 * @returns Generated file path string.
 */
export function generateFilePath(
    bucket: string,
    userId: string,
    originalName: string,
    prefix?: string,
): string {
    const safeName = sanitizeFileName(originalName);
    const timestamp = Date.now();
    const filename = `${timestamp}-${safeName}`;
    return prefix
        ? `${bucket}/${userId}/${prefix}/${filename}`
        : `${bucket}/${userId}/${filename}`;
}

/**
 * File validation options interface.
 */
export interface FileValidationOptions {
    /**
     * Maximum allowed file size in bytes.
     */
    maxSizeBytes: number;
    /**
     * Array of allowed MIME types.
     */
    allowedMimeTypes: string[];
    /**
     * Maximum number of files allowed.
     */
    maxFiles?: number;
}

/**
 * Validates an array of files against specified criteria.
 * @param files - Array of File objects to validate.
 * @param options - Validation options including max size, allowed MIME types, and max file count.
 * @throws Error if any file fails validation.
 */
export function validateFiles(
    files: File[],
    options: FileValidationOptions,
): void {
    if (options.maxFiles && files.length > options.maxFiles) {
        throw new Error(`Maximum ${options.maxFiles} files allowed`);
    }

    for (const file of files) {
        if (file.size > options.maxSizeBytes) {
            throw new Error(
                `File "${file.name}" exceeds size limit (${
                    (options.maxSizeBytes / 1024 / 1024).toFixed(1)
                } MB)`,
            );
        }
        if (!options.allowedMimeTypes.includes(file.type)) {
            throw new Error(
                `Unsupported file type: "${file.type}" in file "${file.name}"`,
            );
        }
    }
}

/**
 * Uploads multiple files to Supabase Storage.
 * @param storage - Supabase Storage client.
 * @param bucket - The storage bucket name.
 * @param files - Array of File objects to upload.
 * @param userId - ID of the user uploading the files.
 * @param prefix - Optional prefix for file paths.
 * @returns Array of uploaded file paths.
 */
export async function uploadFilesToStorage(
    storage: SupabaseClient["storage"],
    bucket: string,
    files: File[],
    userId: string,
    prefix?: string,
): Promise<string[]> {
    const paths: string[] = [];

    try {
        for (const file of files) {
            const path = generateFilePath(bucket, userId, file.name, prefix);
            const bytes = new Uint8Array(await file.arrayBuffer());

            const { error } = await storage
                .from(bucket)
                .upload(path, bytes, {
                    contentType: file.type,
                    upsert: false,
                });

            if (error) {
                // Clean up any already uploaded files on failure
                if (paths.length > 0) {
                    await storage.from(bucket).remove(paths);
                }
                throw new Error(
                    `Upload failed for "${file.name}": ${error.message}`,
                );
            }
            paths.push(path);
        }
        return paths;
    } catch (err) {
        throw err;
    }
}

/**
 * Creates a JSON response with the given data and status code.
 * @param data - The data to include in the response body.
 * @param status - The HTTP status code (default is 200).
 * @returns A Response object with JSON body.
 */
export function createJsonResponse(
    data: Record<string, unknown>,
    status = 200,
): Response {
    return new Response(JSON.stringify(data), {
        status,
        headers: {
            "Content-Type": "application/json",
            ...corsHeaders,
        },
    });
}

/**
 * Validates the user from the request's authorization header.
 * @param req - The incoming Request object.
 * @returns The authenticated User object.
 * @throws HttpError if the user is not authenticated.
 */
export async function validateUser(req: Request): Promise<User> {
    const supabaseAnon = createAnonSupabaseClient();

    const authHeader = req.headers.get("authorization") ?? "";
    const token = authHeader.replace("Bearer ", "");
    const { data: { user }, error: authError } = await supabaseAnon.auth
        .getUser(
            token,
        );

    if (authError || !user) {
        throw new HttpError("Unauthorized", 401);
    }
    return user;
}
