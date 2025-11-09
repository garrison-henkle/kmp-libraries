## :encoding:image
This module contains a couple decoders I made several years ago for a side project. Copying them into
the monorepo in the hopes they'll be useful one day.

Both the ICO and BMP decoders work perfectly fine, but they really need some refactoring to make their
APIs more consistent, as both files decode to different formats (one always decodes to ByteArrays with
mime type labels, while the other uses either byte arrays or direct creation of Compose ImageBitmaps
from the raw BMP pixel data).


