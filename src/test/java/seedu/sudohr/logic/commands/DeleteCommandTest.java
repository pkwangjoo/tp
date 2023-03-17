package seedu.sudohr.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.sudohr.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.sudohr.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.sudohr.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.sudohr.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.sudohr.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.sudohr.testutil.TypicalDepartments.getTypicalSudoHr;
import static seedu.sudohr.testutil.TypicalDepartments.EMPLOYEE_IN_HUMAN_RESOURCES;

import org.junit.jupiter.api.Test;

import seedu.sudohr.commons.core.Messages;
import seedu.sudohr.commons.core.index.Index;
import seedu.sudohr.model.Model;
import seedu.sudohr.model.ModelManager;
import seedu.sudohr.model.UserPrefs;
import seedu.sudohr.model.employee.Employee;

import java.util.stream.IntStream;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code DeleteCommand}.
 */
public class DeleteCommandTest {

    private Model model = new ModelManager(getTypicalSudoHr(), new UserPrefs());

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Employee personToDelete = model.getFilteredEmployeeList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_EMPLOYEE_SUCCESS, personToDelete);

        ModelManager expectedModel = new ModelManager(model.getSudoHr(), new UserPrefs());
        expectedModel.deleteEmployee(personToDelete);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredEmployeeList().size() + 1);
        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model, Messages.MESSAGE_INVALID_EMPLOYEE_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Employee personToDelete = model.getFilteredEmployeeList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_EMPLOYEE_SUCCESS, personToDelete);

        Model expectedModel = new ModelManager(model.getSudoHr(), new UserPrefs());
        expectedModel.deleteEmployee(personToDelete);
        showNoPerson(expectedModel);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of sudohr book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getSudoHr().getEmployeeList().size());

        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model, Messages.MESSAGE_INVALID_EMPLOYEE_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexCascadeToDepartment_success() {

        Employee employeeToDelete = EMPLOYEE_IN_HUMAN_RESOURCES;

        assertTrue(employeeToDelete != null);
        int indexOfEmployeeToDelete = IntStream.range(0, model.getFilteredEmployeeList().size())
                .filter(i -> model.getFilteredEmployeeList().get(i).equals(employeeToDelete))
                .findFirst()
                .getAsInt();

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_EMPLOYEE_SUCCESS, employeeToDelete);

        Model expectedModel = new ModelManager(model.getSudoHr(), new UserPrefs());
        expectedModel.deleteEmployee(employeeToDelete);

        DeleteCommand deleteCommand = new DeleteCommand(Index.fromZeroBased(indexOfEmployeeToDelete));

        // normal deletion success
        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);

        /*
        Check if employee deletion cascaded to department.

        Checking equality of model does not mean that the cascading succeeded
        as the equality check for department is by name.

        So Checking whether a list of department is equal to other list of department will not check through the employees in the department.
         */
        assertFalse(
                model.getFilteredDepartmentList().stream()
                        .anyMatch(d -> d.getEmployees().contains(employeeToDelete))
        );


    }

    @Test
    public void equals() {
        DeleteCommand deleteFirstCommand = new DeleteCommand(INDEX_FIRST_PERSON);
        DeleteCommand deleteSecondCommand = new DeleteCommand(INDEX_SECOND_PERSON);

        // same object -> returns true
        assertTrue(deleteFirstCommand.equals(deleteFirstCommand));

        // same values -> returns true
        DeleteCommand deleteFirstCommandCopy = new DeleteCommand(INDEX_FIRST_PERSON);
        assertTrue(deleteFirstCommand.equals(deleteFirstCommandCopy));

        // different types -> returns false
        assertFalse(deleteFirstCommand.equals(1));

        // null -> returns false
        assertFalse(deleteFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(deleteFirstCommand.equals(deleteSecondCommand));
    }

    /**
     * Updates {@code model}'s filtered list to show no one.
     */
    private void showNoPerson(Model model) {
        model.updateFilteredEmployeeList(p -> false);

        assertTrue(model.getFilteredEmployeeList().isEmpty());
    }
}
