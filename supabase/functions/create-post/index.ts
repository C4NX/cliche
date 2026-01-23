// Edge Function: create post with media (using shared utils + full Zod validation)

import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { z } from "npm:zod";
import {
  createAdminSupabaseClient,
  createJsonResponse,
  serve,
  uploadFilesToStorage,
  validateUser,
} from "../_shared/utils.ts";

const MB = 1024 * 1024;
const MAX_FILE_SIZE = 5 * MB;
const MAX_MEDIA_FILES = 10;
const ALLOWED_MIME_TYPES = ["image/jpeg", "image/png"];
const BUCKET_NAME = "posts";

const CreatePostSchema = z.object({
  caption: z.string().max(500).optional(),
  media: z
    .array(
      z.instanceof(File)
        .refine((file) => {
          return file.size > 0;
        }, {
          message: "File is empty",
        })
        .refine((file) => file.size <= MAX_FILE_SIZE, {
          message: `File size must not exceed ${MAX_FILE_SIZE / MB} MB`,
        })
        .refine((file) => ALLOWED_MIME_TYPES.includes(file.type), {
          message: `Invalid file type. Allowed: ${
            ALLOWED_MIME_TYPES.join(", ")
          }`,
        }),
    )
    .min(1, "At least one media file is required")
    .max(MAX_MEDIA_FILES, `Maximum ${MAX_MEDIA_FILES} media files allowed`),
});

serve(async (req: Request) => {
  const formData = await req.formData();
  const rawCaption = formData.get("caption")?.toString();
  const rawMedia = formData.getAll("media").filter((f): f is File =>
    f instanceof File
  );

  const { caption, media } = await CreatePostSchema.parseAsync({
    caption: rawCaption,
    media: rawMedia,
  });

  const user = await validateUser(req);

  const adminSupabase = createAdminSupabaseClient();
  let uploadedPaths: string[] = [];

  try {
    uploadedPaths = await uploadFilesToStorage(
      adminSupabase.storage,
      BUCKET_NAME,
      media,
      user.id,
    );

    const { error: dbError } = await adminSupabase
      .from("posts")
      .insert({
        caption: caption ?? null,
        owner_id: user.id,
        media_paths: uploadedPaths,
      });

    if (dbError) {
      throw new Error(`Database insert failed: ${dbError.message}`);
    }

    return createJsonResponse(
      {
        message: "Post created successfully",
      },
      201,
    );
  } catch (err) {
    // Cleanup uploaded files in case of any error
    if (uploadedPaths.length > 0) {
      await adminSupabase.storage.from(BUCKET_NAME).remove(uploadedPaths).catch(
        console.error,
      );
    }

    throw err;
  }
});
