@echo off

for %%f in ("*.java") do (
    copy "%%f" "txtfiles\%%f.txt"
)

echo All .java files have been copied and renamed to .txt in the destination folder.
