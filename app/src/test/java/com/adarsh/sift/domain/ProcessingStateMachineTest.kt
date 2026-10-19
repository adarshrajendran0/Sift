package com.adarsh.sift.domain

import com.adarsh.sift.data.Status
import org.junit.Assert.assertEquals
import org.junit.Test

class ProcessingStateMachineTest {

    @Test
    fun testTransitions() {
        val tm = ProcessingStateMachine
        assertEquals(Step.WorthKeeping, tm.transition(Step.Actionable, Answer.No))
        assertEquals(Step.SingleOrProject, tm.transition(Step.Actionable, Answer.Yes))
        
        assertEquals(Step.Resolved(Status.TRASHED), tm.transition(Step.WorthKeeping, Answer.Trash))
        assertEquals(Step.NeedsActionLater, tm.transition(Step.WorthKeeping, Answer.Keep))
        
        assertEquals(Step.Resolved(Status.SOMEDAY), tm.transition(Step.NeedsActionLater, Answer.Someday))
        assertEquals(Step.Resolved(Status.REFERENCE), tm.transition(Step.NeedsActionLater, Answer.Reference))
        
        assertEquals(Step.ProjectOutcome, tm.transition(Step.SingleOrProject, Answer.Project))
        assertEquals(Step.Clarify, tm.transition(Step.SingleOrProject, Answer.SingleAction))
        
        assertEquals(Step.Clarify, tm.transition(Step.ProjectOutcome, Answer.Next))
        
        assertEquals(Step.DoingNow, tm.transition(Step.Clarify, Answer.DoItNow))
        assertEquals(Step.Destination, tm.transition(Step.Clarify, Answer.Later))
        
        assertEquals(Step.Resolved(Status.DONE), tm.transition(Step.DoingNow, Answer.Done))
        assertEquals(Step.Destination, tm.transition(Step.DoingNow, Answer.NeedMoreTime))
        
        assertEquals(Step.Resolved(Status.NEXT_ACTION), tm.transition(Step.Destination, Answer.ContextChip))
        assertEquals(Step.PickDate, tm.transition(Step.Destination, Answer.Schedule))
        assertEquals(Step.PickDelegate, tm.transition(Step.Destination, Answer.Delegate))
        
        assertEquals(Step.Resolved(Status.CALENDAR), tm.transition(Step.PickDate, Answer.PickDateDone))
        
        assertEquals(Step.Resolved(Status.WAITING_FOR), tm.transition(Step.PickDelegate, Answer.PickDelegateDone))
    }

    @Test
    fun testStalledNudge() {
        val tm = ProcessingStateMachine
        assertEquals(Step.Resolved(Status.SOMEDAY), tm.transition(Step.StalledNudge, Answer.Someday))
        assertEquals(Step.Actionable, tm.transition(Step.StalledNudge, Answer.KeepTrying))
    }
}
