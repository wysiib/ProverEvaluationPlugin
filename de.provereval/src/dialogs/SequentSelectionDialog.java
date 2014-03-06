package dialogs;

import java.util.*;
import java.util.List;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eventb.core.IPOSequent;

import de.provereval.Activator;
import de.provereval.labelproviders.SequentsLabelProvider;

public class SequentSelectionDialog extends FilteredItemsSelectionDialog {

	private final List<IPOSequent> allProverSequents;
	private static final String DIALOG_SETTINGS = "FilteredResourcesSelectionDialogExampleSettings";
	private final SequentsLabelProvider labelProvider = new SequentsLabelProvider();

	public SequentSelectionDialog(Shell shell,
			List<IPOSequent> allProverSequents) {
		super(shell, true);
		this.allProverSequents = allProverSequents;

		this.setInitialPattern("**",
				FilteredItemsSelectionDialog.FULL_SELECTION);
		this.setListLabelProvider(labelProvider);
	}

	@Override
	protected Control createExtendedContentArea(Composite parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected IDialogSettings getDialogSettings() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings()
				.getSection(DIALOG_SETTINGS);
		if (settings == null) {
			settings = Activator.getDefault().getDialogSettings()
					.addNewSection(DIALOG_SETTINGS);
		}
		return settings;
	}

	@Override
	protected IStatus validateItem(Object item) {
		return Status.OK_STATUS;
	}

	@Override
	protected ItemsFilter createFilter() {
		return new ItemsFilter() {
			@Override
			public boolean matchItem(Object item) {
				return matches(labelProvider.getText(item));
			}

			@Override
			public boolean isConsistentItem(Object item) {
				return true;
			}
		};
	}

	@Override
	protected Comparator<Object> getItemsComparator() {
		return new Comparator<Object>() {
			@Override
			public int compare(Object arg0, Object arg1) {
				return labelProvider.getText(arg0).compareTo(
						labelProvider.getText(arg1));
			}
		};
	}

	@Override
	protected void fillContentProvider(AbstractContentProvider contentProvider,
			ItemsFilter itemsFilter, IProgressMonitor progressMonitor)
			throws CoreException {
		progressMonitor.beginTask("Searching", allProverSequents.size());
		for (Iterator<IPOSequent> iter = allProverSequents.iterator(); iter
				.hasNext();) {
			contentProvider.add(iter.next(), itemsFilter);
			progressMonitor.worked(1);
		}
		progressMonitor.done();
	}

	@Override
	public String getElementName(Object item) {
		return labelProvider.getText(item);
	}

	public List<IPOSequent> getSelectedProverSequents() {
		List<IPOSequent> selectedSequents = new ArrayList<IPOSequent>();
		for (Object obj : getResult()) {
			selectedSequents.add((IPOSequent) obj);
		}
		return selectedSequents;
	}

}
