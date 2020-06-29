# How to contribute

## Reporting Issues

When considering [reporting an issue/bug][1], first do a thorough search to determine if it has already been addressed or a workaround has been described. Interactions between users and developers occur in multiple places:

* [GitLab Issue Tracker][2] For use when you are relatively certain that a developer's attention is required (don't forget to search the closed reports in addition to those currently open, first)
* [Kodi tMM sub-forum][3] If you aren't certain if your issue is due to a peculiarity in your setup or an actual flaw in the program, creating a thread here will often allow more experienced users to help you to make that determination.
* [tMM Docs][4] and [tMM FAQ][5] Answers to questions/misunderstandings that arise with some frequency will often receive a definitive treatment here

If your search fails to yield anything pertinent or you feel that your experience requires reexamining an acknowledged issue at more length, provide as much information as possible, including at a minimum:

* A short (⩽ 100 characters) but informative title that helps lead others with the same problem to your report
* tinyMediaManager version (e.g. 3.1.2)
* OS & version (e.g. Windows 10 Pro Version 1809 or Manjaro Linux KDE 18.0, Kernel 4.19.28)
* System architecture (e.g. x86 or ARM, 32 or 64-bit)
* Java version (e.g. Oracle Java 8u192)
* Detailed description of the issue (attach screenshots if needed)
* Steps to reproduce the issue

## Code contributions

If you wish to propose an addition or change to the source code, the first step is to create your own copy of the existing codebase in a process known as ["forking."][7] Once you have created your own fork of the repository you are able to make any changes you wish in it and test them to ensure they accomplish the desired result without impacting existing functions. If you feel your work is ready for a developer to review for inclusion, create a [merge request][8] **targeting the devel branch**. Choose a brief title referring to the intended change in behavior and any important details or arguments for inclusion provided in the description field.

Commit messages are sourced directly into the [Changelog][9], requiring them to be in common English, accurate and succinct. Changes made by atomic-style commits that are too granular/numerous to be suitable for the Changelog should select the option to "squash" them into a single commit when creating the merge request.

## Adding translations

If you want to help us translating tinyMediaManager, please register at [Weblate][10].

## Feature requests

Feature requests are also accepted via the [Issue Tracker][2] when made politely and a good case is made for how it will benefit a relatively broad portion of the userbase.

[1]: https://gitlab.com/tinyMediaManager/tinyMediaManager/issues/new?issue
[2]: https://gitlab.com/tinyMediaManager/tinyMediaManager/issues?scope=all&state=all
[3]: https://forum.kodi.tv/forumdisplay.php?fid=204
[4]: https://www.tinymediamanager.org/docs/
[5]: https://www.tinymediamanager.org/help/faq
[7]: https://gitlab.com/tinyMediaManager/tinyMediaManager/forks/new
[8]: https://gitlab.com/tinyMediaManager/tinyMediaManager/merge_requests
[9]: https://gitlab.com/tinyMediaManager/tinyMediaManager/blob/devel/changelog.txt
[10]: https://hosted.weblate.org/projects/tinymediamanager/
