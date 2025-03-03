package travelator.tablereader

import java.io.StringReader

class Table(
    val headers: List<String>,
    val records: Sequence<Map<String, String>>
) : Sequence<Map<String, String>> by records

fun readTableWithHeader(
    lines: Sequence<String>,
    splitter: (String) -> List<String> = splitOnComma
): Table =
    lines.destruct()?.let { (first, rest) ->
        tableOf(splitter, first, rest)
    } ?: Table(emptyList(), emptySequence())

private fun tableOf(
    splitter: (String) -> List<String>,
    first: String,
    rest: Sequence<String>
): Table {
    val headers = splitter(first)
    val sequence = readTable(
        lines = rest,
        headerProvider = headers::get,
        splitter = splitter
    )
    return Table(headers, sequence)
}


fun headerProviderFrom(
    header: String,
    splitter: (String) -> List<String>
): (Int) -> String {
    val headers = splitter(header)
    return { index -> headers[index] }
}

fun readTable(
    lines: Sequence<String>,
    headerProvider: (Int) -> String = Int::toString,
    splitter: (String) -> List<String> = splitOnComma
): Sequence<Map<String, String>> = lines.map {
    parseLine(it, headerProvider, splitter)
}

fun ((String) -> List<String>).readTableWithHeader(
    reader: StringReader
): Sequence<Map<String, String>> =
    readTableWithHeader(reader.buffered().lineSequence(), this)

fun splitOn(separators: String) = { line: String ->
    line.splitFields(separators)
}

val splitOnComma: (String) -> List<String> = splitOn(",")

val splitOnTab: (String) -> List<String> = splitOn("\t")

private fun parseLine(
    line: String,
    headerProvider: (Int) -> String,
    splitter: (String) -> List<String>
): Map<String, String> {
    val values = splitter(line)
    val keys = values.indices.map(headerProvider)
    return keys.zip(values).toMap()
}

// 빈 문자열에 대해 String.split을 호출하면 빈 리스트가 아니라
// 빈 문자열의 리스트를 반환하기 때문에 빈 문자열을 별도로 처리할 필요가 있다.
private fun String.splitFields(separator: String): List<String> =
    if (isEmpty()) emptyList() else split(separator)

fun <T> Sequence<T>.destruct(): Pair<T, Sequence<T>>? {
    val iterator = this.iterator()
    return when {
        iterator.hasNext() ->
            iterator.next() to iterator.asSequence()

        else -> null
    }
}