UPDATE public.outbox
SET status = 0,
    published_at = NULL;

ALTER TABLE public.balance_projection_progress
RENAME COLUMN last_version TO last_processed_version;