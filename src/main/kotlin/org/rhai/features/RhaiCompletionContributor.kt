package org.rhai.features

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import org.rhai.RhaiTypes

class RhaiCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(RhaiTypes.IDENTIFIER),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val keywords = listOf(
                        "let", "if", "else", 
                        "while", "true", "false", "null"
                    )
                    
                    keywords.forEach { keyword ->
                        result.addElement(
                            LookupElementBuilder.create(keyword)
                                .withTypeText("keyword")
                                .bold()
                        )
                    }
                    
                    val builtins = listOf(
                        "print", "println", "type_of", "to_string",
                        "to_int", "to_float", "abs", "sin", "cos"
                    )
                    
                    builtins.forEach { func ->
                        result.addElement(
                            LookupElementBuilder.create(func)
                                .withTypeText("builtin function")
                        )
                    }
                }
            }
        )
    }
}
