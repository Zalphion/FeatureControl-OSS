package com.zalphion.featurecontrol.storage

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.zalphion.featurecontrol.lib.toBiDiMapping
import dev.andrewohara.utils.pagination.Page
import dev.forkhandles.values.StringValue
import dev.forkhandles.values.StringValueFactory
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.http4k.lens.BiDiMapping
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

private class TestGroupId private constructor(value: String): StringValue(value) {
    companion object: StringValueFactory<TestGroupId>(::TestGroupId)
}

private class TestItemId private constructor(value: String): StringValue(value) {
    companion object: StringValueFactory<TestItemId>(::TestItemId)
}

private object TestDocumentJsonAdapter: JsonAdapter<TestDocument>() {
    override fun fromJson(reader: JsonReader): TestDocument {
        var string: String? = null
        var int: Int? = null

        reader.beginObject()
        while(reader.hasNext()) {
            when(reader.nextName()) {
                "string" -> string = reader.nextString()
                "int" -> int = reader.nextInt()
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return TestDocument(string!!, int!!)
    }

    override fun toJson(writer: JsonWriter, doc: TestDocument?) {
        if (doc == null) {
            writer.nullValue()
            return
        }

        writer.beginObject()
        writer.name("string").value(doc.string)
        writer.name("int").value(doc.int)
        writer.endObject()
    }
}

data class TestDocument(
    val string: String,
    val int: Int
) {
    companion object {
        private val adapter = Moshi.Builder()
            .add { type, _, _ -> if (type == TestDocument::class.java) TestDocumentJsonAdapter else null }
            .build()
            .adapter(TestDocument::class.java)

        val mapping = BiDiMapping(
            TestDocument::class.java,
            asIn = { adapter.toJson(it) },
            asOut = { adapter.fromJson(it)!! }
        )
    }
}

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
abstract class StorageDriverContract(storageDriverFn: (PageSize) -> StorageDriver) {

    private val repo = storageDriverFn(PageSize.of(2)).create(
        collectionName = "test",
        groupIdMapper = TestGroupId.toBiDiMapping(),
        itemIdMapper = TestItemId.toBiDiMapping(),
        documentMapper = TestDocument.mapping
    )

    private val group1 = TestGroupId.parse("group1")
    private val group2 = TestGroupId.parse("group2")
    private val group3 = TestGroupId.parse("group3")
    private val group4 = TestGroupId.parse("group4")

    private val item1 = TestItemId.parse("item1")
    private val item2 = TestItemId.parse("item2")
    private val item3 = TestItemId.parse("item3")
    private val item4 = TestItemId.parse("item4")

    private val doc1 = TestDocument("one", 1)
    private val doc2 = TestDocument("two", 2)
    private val doc3 = TestDocument("three", 3)
    private val doc4 = TestDocument("four", 4)

    @Test
    fun `get - not found`() {
        repo[group1, item1] shouldBe null
    }

    @Test
    fun `get - found`() {
        repo.save(group1, item1, doc1)
        repo.save(group1, item2, doc2)
        repo.save(group2, item3, doc3)
        repo.save(group2, item4, doc4)

        repo[group1, item1] shouldBe doc1
        repo[group2, item4] shouldBe doc4
    }

    @Test
    fun `delete - not found`() {
        repo.delete(group1, item1) // should gracefully fail
    }

    @Test
    fun `delete - found`() {
        repo.save(group1, item1, doc1)
        repo.save(group1, item2, doc2)
        repo.save(group2, item3, doc3)
        repo.save(group2, item4, doc4)

        repo.delete(group1, item2)

        repo[group1, item1] shouldBe doc1
        repo[group1, item2] shouldBe null
    }

    @Test
    fun `batch get`() {
        repo.save(group1, item1, doc1)
        repo.save(group1, item2, doc2)
        repo.save(group2, item3, doc3)
        repo.save(group2, item4, doc4)

        repo[listOf(
            group1 to item1, // hit
            group1 to item3, // miss
            group2 to item3 // hit
        )].shouldContainExactly(doc1, doc3)
    }

    @Test
    fun list() {
        repo.save(group1, item1, doc1)
        repo.save(group1, item2, doc2)
        repo.save(group1, item3, doc3)
        repo.save(group2, item4, doc4)

        val paginator = repo.list(group1)
        paginator[null] shouldBe Page(listOf(doc1, doc2), item2)
        paginator[item2] shouldBe Page(listOf(doc3), null)
    }

    @Test
    fun `list - inverse`() {
        repo.save(group1, item1, doc1)
        repo.save(group2, item1, doc2)
        repo.save(group3, item1, doc3)
        repo.save(group4, item2, doc4)

        val paginator = repo.listInverse(item1)
        paginator[null] shouldBe Page(listOf(doc1, doc2), group2)
        paginator[group2] shouldBe Page(listOf(doc3), null)
    }
}