package per.goweii.codex.decoder

import per.goweii.codex.CodeProcessor
import per.goweii.codex.CodeResult

interface DecodeProcessor<Input> : CodeProcessor<Input, List<CodeResult>> {
    override fun process(
        input: Input,
        onSuccess: (List<CodeResult>) -> Unit,
        onFailure: (Throwable) -> Unit
    )
}