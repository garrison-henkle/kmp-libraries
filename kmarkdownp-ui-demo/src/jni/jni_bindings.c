#include <jni.h>
#include "grammar.h"

#ifndef __ANDROID__
#define NATIVE_FUNCTION(name) JNIEXPORT jlong JNICALL name(JNIEnv * _env, jclass _class)
#else
#define NATIVE_FUNCTION(name) JNIEXPORT jlong JNICALL name()
#endif

NATIVE_FUNCTION(Java_dev_henkle_markdown_ui_grammars_TreeSitterMarkdown_treeSitterMarkdown) {
    return (jlong) tree_sitter_markdown();
}

NATIVE_FUNCTION(Java_dev_henkle_markdown_ui_grammars_TreeSitterMarkdown_treeSitterMarkdownInline) {
    return (jlong) tree_sitter_markdown_inline();
}
