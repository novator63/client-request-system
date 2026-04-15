UPDATE tickets
SET category_id = (
	SELECT id
	FROM categories
	ORDER BY id
	LIMIT 1
)
WHERE category_id IS NULL;

ALTER TABLE tickets
ALTER COLUMN category_id SET NOT NULL;