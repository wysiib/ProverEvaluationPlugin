package de.provereval;

import java.util.List;

import org.eventb.core.IPOSequent;
import org.eventb.core.preferences.IPrefMapEntry;
import org.eventb.core.seqprover.ITacticDescriptor;

public class ProverEvaluationTaskList {

	private final List<IPOSequent> sequents;
	private final List<IPrefMapEntry<ITacticDescriptor>> reasoners;

	public ProverEvaluationTaskList(List<IPOSequent> sequents,
			List<IPrefMapEntry<ITacticDescriptor>> reasoners) {
		this.sequents = sequents;
		this.reasoners = reasoners;
	}

	public int size() {
		return sequents.size() * reasoners.size();
	}

	public ProverEvaluationTask get(int i) {
		IPrefMapEntry<ITacticDescriptor> reasoner = reasoners.get(i
				% reasoners.size());
		IPOSequent sequent = sequents.get(i / reasoners.size());
		return new ProverEvaluationTask(reasoner, sequent);
	}

}
