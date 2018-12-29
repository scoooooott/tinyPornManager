#!/bin/bash
rm changelog.txt
DATE=$(date +%F)
START_DATE==$(date --date="-2 month" +'%Y-%m-%d 00:00:00')
git log --no-merges --format="%cd" --date=short --invert-grep --grep="Translated using Weblate" --since="$START_DATE" | sort -u -r | while read DATE ; do
  echo >> changelog.txt
  echo [$DATE] >> changelog.txt
  GIT_PAGER=cat git log --no-merges --format=" * %s" --since="$DATE 00:00:00" --until="$DATE 23:59:59" --invert-grep --grep="Translated using Weblate" >> changelog.txt
done
