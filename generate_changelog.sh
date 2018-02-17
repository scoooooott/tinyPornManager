#!/bin/bash
rm changelog.txt
DATE=$(date +%F)
git log --no-merges --format="%cd" --date=short --invert-grep --grep="Translated using Weblate" --since="2016-01-20 00:00:00" v3 | sort -u -r | while read DATE ; do
  echo >> changelog.txt
  echo [$DATE] >> changelog.txt
  GIT_PAGER=cat git log --no-merges --format=" * %s" --since="$DATE 00:00:00" --until="$DATE 23:59:59" --invert-grep --grep="Translated using Weblate" v3 >> changelog.txt
done
