```sh
  ___     ___  ____    ___   ___     ___      __
|    \   /  _]|    \  /   \ |   \   /   \    /  ]
|  D  ) /  [_ |  o  )|     ||    \ |     |  /  /
|    / |    _]|   _/ |  O  ||  D  ||  O  | /  /  
|    \ |   [_ |  |   |     ||     ||     |/   \_
|  .  \|     ||  |   |     ||     ||     |\     |
|__|\_||_____||__|    \___/ |_____| \___/  \____|
                                            v 0.1
```                                           
### Under Construction.
#### What it can do,for now:
1. Can create a PDF file from any local Git repos' "readable" files.
2. Create an index at the end of the PDF file(can be found at last pages of generated PDF document).

#### What it can't do, for now(mostly due to laziness reasons ;P ):
1. Can't parse image files, pdf files inside of Repo.
2. Can't parse binary files because of font's glyph limitation.
3. Text-wrapping not supported as of now. Will do in future versions.

All configurations can be done inside conf.properties like path to repo,
file extensions to ignore, folders to ignore and output folder.
#### Example of a page in the document:

<img src="https://github.com/Anulav/GitConfig/blob/main/RepoDoCfile.png">

#### Example of Index in the document:
<img src="https://github.com/Anulav/GitConfig/blob/main/RepoDoCIndexFile.png">


