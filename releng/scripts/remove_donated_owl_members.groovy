package scripts

import org.eclipse.emf.cdo.common.id.CDOIDUtil

import com.b2international.snowowl.datastore.BranchPathUtils
import com.b2international.snowowl.snomed.datastore.SnomedEditingContext

def idsToDelete = [432345564243648476L, 432345564243647202L]

def branch = "MAIN/2020-01-31/SNOMEDCT-US"

def editingContext = new SnomedEditingContext(BranchPathUtils.createPath(branch))

idsToDelete.each { id ->
	editingContext.getTransaction().getLastSavepoint().getDetachedObjects().put(CDOIDUtil.createLong(id), editingContext.lookupIfExists(id))
}

editingContext.commit("Remove donated OWL member(s)")

editingContext.close()
