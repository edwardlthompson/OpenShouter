package org.openshouter.ui.feedback

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import dev.foss.goldenpath.R
import org.openshouter.feedback.FeedbackPreview
import org.openshouter.githubfeedback.IssueFormUrl
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuScaffold
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection
import org.openshouter.ui.menu.rememberMenuScrollStore

@Composable
fun FeedbackScreen(
    kind: String,
    releaseRepo: String,
    stack: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    scrollStore: MenuScrollStore = rememberMenuScrollStore(),
) {
    var description by remember { mutableStateOf("") }
    val preview = FeedbackPreview.text(kind, description, stack)
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val canSubmit = FeedbackPreview.canSubmit(description, stack)
    val titleRes = when {
        !stack.isNullOrBlank() && kind == "bug" -> R.string.feedback_crash_title
        kind == "feature" -> R.string.feedback_feature_title
        else -> R.string.feedback_bug_title
    }
    MenuScaffold(stringResource(titleRes), scrollStore, "feedback", onBack, modifier) {
        MenuSection(stringResource(R.string.feedback_description)) {
            MenuBody {
                Text(text = stringResource(R.string.feedback_clipboard_hint))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.feedback_description)) },
                )
                Text(text = preview, style = MaterialTheme.typography.bodySmall)
                Button(onClick = { clipboard.setText(AnnotatedString(preview)) }) {
                    Text(stringResource(R.string.feedback_copy))
                }
                Button(
                    enabled = canSubmit,
                    onClick = {
                        openGithub(context, clipboard, kind, description, preview, releaseRepo)
                    },
                ) {
                    Text(stringResource(R.string.feedback_open))
                }
                Button(
                    onClick = {
                        clipboard.setText(AnnotatedString(""))
                        onBack()
                    },
                ) {
                    Text(stringResource(R.string.feedback_discard))
                }
            }
        }
    }
}

private fun openGithub(
    context: android.content.Context,
    clipboard: androidx.compose.ui.platform.ClipboardManager,
    kind: String,
    description: String,
    preview: String,
    releaseRepo: String,
) {
    val template = if (kind == "feature") "product_idea.yml" else "bug_report.yml"
    val fields = if (kind == "feature") {
        mapOf("problem" to description, "solution" to preview, "title" to "[feat]: ")
    } else {
        mapOf("description" to description, "reproduction" to preview, "title" to "[bug]: ")
    }
    val built = IssueFormUrl.build(releaseRepo, template, fields)
    if (built.bodyTooLarge) {
        clipboard.setText(AnnotatedString(built.clipboardMarkdown ?: preview))
    }
    val url = built.url
    if (url.startsWith("https://")) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }
}
