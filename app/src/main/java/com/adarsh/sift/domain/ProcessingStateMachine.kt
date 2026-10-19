package com.adarsh.sift.domain

import com.adarsh.sift.data.Status
import java.time.Instant

sealed interface Step {
    data object StalledNudge : Step
    data object Actionable : Step
    data object WorthKeeping : Step
    data object NeedsActionLater : Step
    data object SingleOrProject : Step
    data object ProjectOutcome : Step
    data object Clarify : Step
    data object DoingNow : Step
    data object Destination : Step
    data object PickDate : Step
    data object PickDelegate : Step
    data class Resolved(val status: Status) : Step
}

sealed interface Answer {
    data object KeepTrying : Answer
    data object Yes : Answer
    data object No : Answer
    data object Keep : Answer
    data object Trash : Answer
    data object Someday : Answer
    data object Reference : Answer
    data object SingleAction : Answer
    data object Project : Answer
    data object Next : Answer
    data object DoItNow : Answer
    data object Later : Answer
    data object Done : Answer
    data object NeedMoreTime : Answer
    data object ContextChip : Answer
    data object Schedule : Answer
    data object Delegate : Answer
    data object PickDateDone : Answer
    data object PickDelegateDone : Answer
}

object ProcessingStateMachine {
    fun transition(step: Step, answer: Answer): Step {
        return when (step) {
            Step.StalledNudge -> when (answer) {
                Answer.Someday -> Step.Resolved(Status.SOMEDAY)
                Answer.KeepTrying -> Step.Actionable
                else -> error("Invalid answer $answer for $step")
            }
            Step.Actionable -> when (answer) {
                Answer.No -> Step.WorthKeeping
                Answer.Yes -> Step.SingleOrProject
                else -> error("Invalid answer $answer for $step")
            }
            Step.WorthKeeping -> when (answer) {
                Answer.Trash -> Step.Resolved(Status.TRASHED)
                Answer.Keep -> Step.NeedsActionLater
                else -> error("Invalid answer $answer for $step")
            }
            Step.NeedsActionLater -> when (answer) {
                Answer.Someday -> Step.Resolved(Status.SOMEDAY)
                Answer.Reference -> Step.Resolved(Status.REFERENCE)
                else -> error("Invalid answer $answer for $step")
            }
            Step.SingleOrProject -> when (answer) {
                Answer.Project -> Step.ProjectOutcome
                Answer.SingleAction -> Step.Clarify
                else -> error("Invalid answer $answer for $step")
            }
            Step.ProjectOutcome -> when (answer) {
                Answer.Next -> Step.Clarify
                else -> error("Invalid answer $answer for $step")
            }
            Step.Clarify -> when (answer) {
                Answer.DoItNow -> Step.DoingNow
                Answer.Later -> Step.Destination
                else -> error("Invalid answer $answer for $step")
            }
            Step.DoingNow -> when (answer) {
                Answer.Done -> Step.Resolved(Status.DONE)
                Answer.NeedMoreTime -> Step.Destination
                else -> error("Invalid answer $answer for $step")
            }
            Step.Destination -> when (answer) {
                Answer.ContextChip -> Step.Resolved(Status.NEXT_ACTION)
                Answer.Schedule -> Step.PickDate
                Answer.Delegate -> Step.PickDelegate
                else -> error("Invalid answer $answer for $step")
            }
            Step.PickDate -> when (answer) {
                Answer.PickDateDone -> Step.Resolved(Status.CALENDAR)
                else -> error("Invalid answer $answer for $step")
            }
            Step.PickDelegate -> when (answer) {
                Answer.PickDelegateDone -> Step.Resolved(Status.WAITING_FOR)
                else -> error("Invalid answer $answer for $step")
            }
            is Step.Resolved -> step
        }
    }
}
