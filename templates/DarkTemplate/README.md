# Dark Template

Simple template for movies inspired by Emby. It includes:
- search of title
- download link for media and subtitle (see instructions below) 
- simple responsive design

![](https://buron.coffee/files/darkTemplate/preview.webm)

## Setup for download links

Copy the directories hierarchy from the root directory to our movie folder in the export directory and create a symlink to our movie folder.

```bash
# our movies folder have the following absolute path
# /parent_path/movies
cd export
mkdir -p parent_path
cd parent_path
ln -s /parent_path/movies movies
```


