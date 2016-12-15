/*
 * Copyright 2011-2015 B2i Healthcare Pte Ltd, http://b2i.sg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.b2international.snowowl.datastore.server.internal.branch;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.util.CDOTimeProvider;
import org.eclipse.emf.cdo.spi.common.branch.InternalCDOBranch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.b2international.index.Index;
import com.b2international.index.IndexWrite;
import com.b2international.index.Indexes;
import com.b2international.index.mapping.Mappings;
import com.b2international.snowowl.core.MetadataHolder;
import com.b2international.snowowl.core.MetadataImpl;
import com.b2international.snowowl.core.ServiceProvider;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.branch.BranchManager;
import com.b2international.snowowl.core.domain.RepositoryContext;
import com.b2international.snowowl.core.domain.RepositoryContextProvider;
import com.b2international.snowowl.datastore.oplock.impl.IDatastoreOperationLockManager;
import com.b2international.snowowl.datastore.review.ReviewManager;
import com.b2international.snowowl.datastore.server.cdo.ICDOConflictProcessor;
import com.b2international.snowowl.datastore.server.internal.InternalRepository;
import com.b2international.snowowl.datastore.server.internal.JsonSupport;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * @since 4.1
 */
@RunWith(MockitoJUnitRunner.class)
public class CDOBranchManagerTest {

	private CDOTimeProvider clock;
	private MockInternalCDOBranchManager cdoBranchManager;
	
	private CDOBranchManagerImpl manager;
	
	private InternalRepository repository;
	private ServiceProvider context;
	private Index store;
	
	@Before
	public void givenCDOBranchManager() {
		clock = new AtomicLongTimestampAuthority();
		cdoBranchManager = new MockInternalCDOBranchManager(clock);
		cdoBranchManager.initMainBranch(false, clock.getTimeStamp());

		repository = mock(InternalRepository.class, RETURNS_MOCKS);
		final ICDOConflictProcessor conflictProcessor = mock(ICDOConflictProcessor.class, RETURNS_DEFAULTS);
		final InternalCDOBranch mainBranch = cdoBranchManager.getMainBranch();
		
		when(repository.getCdoBranchManager()).thenReturn(cdoBranchManager);
		when(repository.getCdoMainBranch()).thenReturn(mainBranch);
		when(repository.getConflictProcessor()).thenReturn(conflictProcessor);
		store = Indexes.createIndex(UUID.randomUUID().toString(), JsonSupport.getDefaultObjectMapper(), new Mappings(CDOMainBranchImpl.class, CDOBranchImpl.class, InternalBranch.class));
		store.admin().create();
		when(repository.getIndex()).thenReturn(store);
		
		manager = new CDOBranchManagerImpl(repository);
		
		context = mock(ServiceProvider.class);
		final RepositoryContextProvider repositoryContextProvider = mock(RepositoryContextProvider.class);
		final RepositoryContext repositoryContext = mock(RepositoryContext.class);
		
		final IDatastoreOperationLockManager lockManager = mock(IDatastoreOperationLockManager.class);
		final ReviewManager reviewManager = mock(ReviewManager.class);
		
		when(repositoryContext.service(IDatastoreOperationLockManager.class)).thenReturn(lockManager);
		when(repositoryContext.service(ReviewManager.class)).thenReturn(reviewManager);
		when(repositoryContext.service(BranchManager.class)).thenReturn(manager);
		
		when(repositoryContextProvider.get(context, repository.id())).thenReturn(repositoryContext);
		when(context.service(RepositoryContextProvider.class)).thenReturn(repositoryContextProvider);
	}
	
	@After
	public void after() {
		store.admin().delete();
	}
	
	@Test
	public void updateMetadataShouldReturnNewInstanceWithProperType() throws Exception {
		final MetadataHolder newMain = manager.getMainBranch().withMetadata(new MetadataImpl());
		assertTrue(newMain instanceof CDOMainBranchImpl);
	}
	
	@Test
	public void whenGettingMainBranch_ThenItShouldBeReturned_AndAssociatedWithItsCDOBranch() throws Exception {
		final Branch main = manager.getMainBranch();
		final CDOBranch cdoBranch = manager.getCDOBranch(main);
		assertEquals(main.path(), cdoBranch.getPathName());
	}
	
	@Test
	public void whenCreatingBranch_ThenItShouldBeCreated_AndACDOBranchShouldBeAssociatedWithIt() throws Exception {
		final Branch a = manager.getMainBranch().createChild("a");
		final CDOBranch cdoBranch = manager.getCDOBranch(a);
		assertEquals(a.path(), cdoBranch.getPathName());
	}
	
	@Test
	public void whenCreatingDeepBranch_ThenItShouldBeCreatedAndAssociatedWithCDOBranches() throws Exception {
		final Branch a = manager.getMainBranch().createChild("a");
		final Branch b = a.createChild("b");
		final CDOBranch cdoBranchA = manager.getCDOBranch(a);
		assertEquals(a.path(), cdoBranchA.getPathName());
		final CDOBranch cdoBranchB = manager.getCDOBranch(b);
		assertEquals(b.path(), cdoBranchB.getPathName());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void whenGettingCDOBranchOfDeletedBranch_ThenThrowException() throws Exception {
		final Branch a = manager.getMainBranch().createChild("a");
		final Branch deletedA = a.delete();
		manager.getCDOBranch(deletedA);
	}
	
	@Test
	public void whenRebasingChildBranchInForwardState_ThenManagerShouldReopenAssociatedCDOBranch() throws Exception {
		final Branch a = manager.getMainBranch().createChild("a");
		final CDOBranch cdoBranchA = manager.getCDOBranch(a);

		// commit and rebase
		manager.handleCommit((InternalBranch) a.parent(), clock.getTimeStamp());
		
		final Branch rebasedBranchA = a.rebase(a.parent(), "Rebase");
		final CDOBranch rebasedCdoBranchA = manager.getCDOBranch(rebasedBranchA);
		assertNotEquals(rebasedCdoBranchA.getID(), cdoBranchA.getID());
	}
	
	@Test
	public void whenCreatingBranchThenAssignNewSegmentsToParentAndChild() throws Exception {
		final InternalCDOBasedBranch a = (InternalCDOBasedBranch) manager.getMainBranch().createChild("a");
		final InternalCDOBasedBranch parent = (InternalCDOBasedBranch) a.parent();
		
		assertThat(a.segmentId()).isEqualTo(1);
		assertThat(a.segments()).containsOnly(1);
		assertThat(a.parentSegments()).containsOnly(0);
		
		assertThat(parent.segmentId()).isEqualTo(2);
		assertThat(parent.segments()).containsOnly(0, 2);
		assertThat(parent.parentSegments()).isEmpty();
	}
	
	@Test
	public void whenRebasingChildBranchReassignSegments() throws Exception {
		final Branch a = manager.getMainBranch().createChild("a");
		// make a commit on MAIN
		manager.handleCommit((InternalBranch) a.parent(), clock.getTimeStamp());
		// rebase child
		final InternalCDOBasedBranch rebasedA = (InternalCDOBasedBranch) a.rebase(a.parent(), "Rebase A");
		final InternalCDOBasedBranch parentAfterRebase = (InternalCDOBasedBranch) rebasedA.parent();
		assertThat(rebasedA.segmentId()).isEqualTo(3);
		assertThat(rebasedA.segments()).containsOnly(3);
		assertThat(rebasedA.parentSegments()).containsOnly(0, 2);
		
		assertThat(parentAfterRebase.segmentId()).isEqualTo(4);
		assertThat(parentAfterRebase.segments()).containsOnly(0, 2, 4);
		assertThat(parentAfterRebase.parentSegments()).isEmpty();
	}
	
	@Test
	public void whenCreatingDeepBranchAssignCorrectSegments() throws Exception {
		final InternalCDOBasedBranch c = (InternalCDOBasedBranch) manager.getMainBranch().createChild("a").createChild("b").createChild("c");
		
		final InternalCDOBasedBranch a = (InternalCDOBasedBranch) manager.getBranch("MAIN/a");
		assertThat(a.segmentId()).isEqualTo(4);
		assertThat(a.segments()).containsOnly(4, 1);
		assertThat(a.parentSegments()).containsOnly(0);

		final InternalCDOBasedBranch b = (InternalCDOBasedBranch) manager.getBranch("MAIN/a/b");
		assertThat(b.segmentId()).isEqualTo(6);
		assertThat(b.segments()).containsOnly(6, 3);
		assertThat(b.parentSegments()).containsOnly(0, 1);
		
		assertThat(c.segmentId()).isEqualTo(5);
		assertThat(c.segments()).containsOnly(5);
		assertThat(c.parentSegments()).containsOnly(0, 1, 3);
	}

	@Test
	public void whenCreatingBranch_ThenMetadataOnMainShouldBePreserved() throws Exception {
		manager.getMainBranch().update(new MetadataImpl(ImmutableMap.<String, Object>of("key", "value")));
		final Branch a = manager.getMainBranch().createChild("a");
		assertEquals(ImmutableMap.<String, Object>of("key", "value"), a.parent().metadata());
	}
	
	@Test
	public void whenCreatingNestedBranch_ThenMetadataOnParentShouldBePreserved() throws Exception {
		manager.getMainBranch().createChild("a");
		manager.getBranch("MAIN/a").update(new MetadataImpl(ImmutableMap.<String, Object>of("key", "value")));	
		final Branch b = manager.getBranch("MAIN/a").createChild("b");
		assertEquals(ImmutableMap.<String, Object>of("key", "value"), b.parent().metadata());
	}
	
	@Test
	public void updateMetadata() throws Exception {
		final InternalBranch branchA = (InternalBranch) manager.getMainBranch().createChild("a", new MetadataImpl(ImmutableMap.<String, Object>of("test", 0)));
		final long commitTimestamp = clock.getTimeStamp();
		final IndexWrite<Void> timestampUpdate = manager.update(branchA.getClass(), branchA.path(), new Function<InternalBranch, InternalBranch>() {
			@Override
			public InternalBranch apply(InternalBranch input) {
				return input.withHeadTimestamp(commitTimestamp);
			}
		});
		
		final IndexWrite<Void> metadataUpdate = manager.update(branchA.getClass(), branchA.path(), new Function<InternalBranch, InternalBranch>() {
			@Override
			public InternalBranch apply(InternalBranch input) {
				return input.withMetadata(new MetadataImpl(ImmutableMap.<String, Object>of("test", 1)));
			}
		});
		final Collection<IndexWrite<Void>> parallelUpdates = ImmutableList.of(timestampUpdate, metadataUpdate);
		
		final CyclicBarrier barrier = new CyclicBarrier(parallelUpdates.size());
		final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(parallelUpdates.size()));
		final Collection<ListenableFuture<?>> futures = newArrayList();
		for (final IndexWrite<Void> parallelUpdate : parallelUpdates) {
			futures.add(executor.submit(new Runnable() {
				@Override
				public void run() {
					try {
						barrier.await(1000, TimeUnit.MILLISECONDS);
						manager.commit(parallelUpdate);
					} catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
						throw new RuntimeException("Failed to wait for all parties");
					}
				}
			}));
		}
		
		// wait all runnables to complete
		Futures.allAsList(futures).get();

		// after parallel updates, both timestamp and metadata should be changed and recorded
		final Branch branch = manager.getBranch("MAIN/a");
		assertEquals(branch.headTimestamp(), commitTimestamp);
		assertEquals(branch.metadata(), ImmutableMap.<String, Object>of("test", 1));
	}
}
