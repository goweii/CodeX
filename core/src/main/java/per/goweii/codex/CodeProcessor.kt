package per.goweii.codex

interface CodeProcessor<Input, Output> {
    fun process(
        input: Input,
        onSuccess: (Output) -> Unit,
        onFailure: (Exception) -> Unit
    )
}