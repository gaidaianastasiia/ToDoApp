package com.example.todo.presentation.fragment.todolist

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo.R
import com.example.todo.databinding.FragmentToDoListBinding
import com.example.todo.presentation.base.BaseFragment
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlin.reflect.KClass

private const val CREATE_TODO_FRAGMENT_DIALOG_TAG = "CREATE_TODO_FRAGMENT_DIALOG_TAG"
private const val TO_DO_CONTROLS_FRAGMENT_TAG = "TO_DO_CONTROLS_FRAGMENT_TAG"
private const val RECOVER_DELETE_TO_DO_MESSAGE = "To-Do has been removed"

class ToDoListFragment :
    BaseFragment<ToDoListViewModel, ToDoListViewModel.Factory, FragmentToDoListBinding>(),
    ToDoAdapter.ToDoAdapterListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        setObserve()
        setFragmentResultListeners()
        setClickListeners()
        viewModel.requestList()
    }

    override fun onToDoClick(id: Long, doneStatus: Boolean) {
        viewModel.onClick(id, doneStatus)
    }


    override fun onToDoLongClick(id: Long, text: String) {
        viewModel.onLongClick(id)
    }

    private fun setAdapter() {
        val recyclerView = binding.listRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapter = ToDoAdapter(this)
        recyclerView.adapter = adapter

        viewModel.list.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    private fun setObserve() {
        viewModel.showCreateToDoDialog.observe(viewLifecycleOwner) {
            showCreateToDoDialog()
        }

        viewModel.controls.observe(viewLifecycleOwner) {
            showControlsListDialog(it.id, it.text)
        }

        viewModel.showRecoverDeletedTodoMessage.observe(viewLifecycleOwner) {
            showRecoverDeletedTodoMessage(it)
        }

        viewModel.showEmptyState.observe(viewLifecycleOwner) {
            showEmptyState()
        }

        viewModel.hideEmptyState.observe(viewLifecycleOwner) {
            hideEmptyState()
        }

        viewModel.showLoader.observe(viewLifecycleOwner) {
            showLoader()
        }

        viewModel.hideLoader.observe(viewLifecycleOwner) {
            hideLoader()
        }

        viewModel.showErrorMessage.observe(viewLifecycleOwner) {
            showErrorMessage()
        }
    }

    private fun setFragmentResultListeners() {
        setFragmentResultListener(EDIT_TO_DO_DIALOG_RESULT_KEY) { _, _ ->
            viewModel.requestList()
        }

        setFragmentResultListener((DELETE_TO_DO_REQUEST_KEY)) { _, bundle ->
            val id = bundle.getLong(DELETED_TO_DO_ID)
            viewModel.onDeleteRequest(id)
        }
    }

    private fun setClickListeners() {
        binding.addNewToDoFloatingActionButton.setOnClickListener {
            viewModel.onAddNewToDoButtonClick()
        }
    }

    private fun showCreateToDoDialog() {
        val createToDoDialog = ToDoDialogFragment()
        createToDoDialog.show(parentFragmentManager, CREATE_TODO_FRAGMENT_DIALOG_TAG)
    }

    private fun showControlsListDialog(id: Long, text: String) {
        val controlsToDoDialog = ToDoControlsFragment.getInstance(id, text)
        controlsToDoDialog.show(parentFragmentManager, TO_DO_CONTROLS_FRAGMENT_TAG)
    }

    @SuppressLint("ShowToast")
    private fun showRecoverDeletedTodoMessage(id: Long) {
        Snackbar.make(binding.root, RECOVER_DELETE_TO_DO_MESSAGE, Snackbar.LENGTH_LONG)
            .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    if (event == DISMISS_EVENT_TIMEOUT) {
                        viewModel.delete(id)
                    }
                }
            })
            .setAction(R.string.recover_button) { viewModel.requestList() }
            .show()
    }

    private fun showEmptyState() {
        binding.emptyStateTextView.visibility = View.VISIBLE
    }

    private fun hideEmptyState() {
        binding.emptyStateTextView.visibility = View.GONE
    }

    private fun showLoader() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        binding.progressBar.visibility = View.GONE
    }

    override val viewModelClass: KClass<ToDoListViewModel> = ToDoListViewModel::class

    override fun createViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentToDoListBinding = FragmentToDoListBinding.inflate(inflater, parent, false)
}