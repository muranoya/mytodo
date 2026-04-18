package net.meshpeak.mytodo.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Room スキーマ変更時の Migration 検証用テスト。
 *
 * 現状は version 1 のみなので、「エクスポート済みスキーマから空 DB を生成できる」ことだけを検証する。
 * version 2 を切る際は [androidx.room.migration.Migration] を追加し、
 * `helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)` を追加する。
 */
@RunWith(AndroidJUnit4::class)
class MytodoDatabaseMigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        MytodoDatabase::class.java,
    )

    @Test
    fun version1_schemaIsValid() {
        helper.createDatabase(TEST_DB, 1).close()
    }

    companion object {
        private const val TEST_DB = "mytodo-migration-test.db"
    }
}
