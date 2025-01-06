#ifndef GRAMMAR_H_
#define GRAMMAR_H_

typedef struct TSLanguage TSLanguage;

#ifdef __cplusplus
extern "C" {
#endif

const TSLanguage *tree_sitter_markdown(void);
const TSLanguage *tree_sitter_markdown_inline(void);

#ifdef __cplusplus
}
#endif

#endif // GRAMMAR_H_
