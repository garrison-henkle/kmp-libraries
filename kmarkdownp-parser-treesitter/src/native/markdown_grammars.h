#ifndef MARKDOWN_GRAMMARS_H_
#define MARKDOWN_GRAMMARS_H_

typedef struct TSLanguage TSLanguage;

#ifdef __cplusplus
extern "C" {
#endif

const TSLanguage *tree_sitter_markdown(void);
const TSLanguage *tree_sitter_markdown_inline(void);

#ifdef __cplusplus
}
#endif

#endif // MARKDOWN_GRAMMARS_H_
