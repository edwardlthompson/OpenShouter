import sqlite3
import sys

conn = sqlite3.connect(sys.argv[1])
rows = conn.execute(
    "SELECT kind, postedAt, length(spoken), length(title), length(text) "
    "FROM notification_history ORDER BY postedAt DESC LIMIT 25"
).fetchall()
print("rows", len(rows))
for row in rows:
    print(row)
print("TIME", conn.execute("SELECT count(*) FROM notification_history WHERE kind='TIME'").fetchone()[0])
print("CALL", conn.execute("SELECT count(*) FROM notification_history WHERE kind='CALL'").fetchone()[0])
print("MESSAGE", conn.execute("SELECT count(*) FROM notification_history WHERE kind='MESSAGE'").fetchone()[0])
