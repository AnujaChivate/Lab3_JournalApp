import androidx.lifecycle.ViewModel
import com.zybooks.journal.Journal
import java.util.concurrent.CopyOnWriteArrayList

// data class for Journals. The journalList holds list of all created journals
class JournalViewModel(initialList: CopyOnWriteArrayList<Journal> = CopyOnWriteArrayList()) : ViewModel() {
    var journalList: CopyOnWriteArrayList<Journal> = initialList
}