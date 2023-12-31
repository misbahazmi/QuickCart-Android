package com.misbah.quickcart.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.misbah.quickcart.R
import com.misbah.quickcart.core.base.BaseFragment
import com.misbah.quickcart.core.data.model.Order
import com.misbah.quickcart.core.data.storage.SortOrder
import com.misbah.quickcart.databinding.FragmentOrdersBinding
import com.misbah.quickcart.ui.adapters.OrdersAdapter
import com.misbah.quickcart.ui.listeners.OnItemClickListener
import com.misbah.quickcart.ui.main.CartViewModel
import com.misbah.quickcart.ui.main.MainActivity
import com.misbah.quickcart.ui.utils.exhaustive
import com.misbah.quickcart.ui.utils.gone
import com.misbah.quickcart.ui.utils.onQueryTextChanged
import com.misbah.quickcart.ui.utils.visible
import com.nytimes.utils.AppLog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author: Mohammad Misbah
 * @since: 16-DEC-2023
 * @sample: Technology Assessment for Sr. Android Role
 * Email Id: mohammadmisbahazmi@gmail.com
 * GitHub: https://github.com/misbahazmi
 * Expertise: Android||Java/Kotlin||Flutter
 */
class OrderListFragment :  BaseFragment<OrderListViewModel>(), OnItemClickListener {
    private var _binding: FragmentOrdersBinding? = null
    internal lateinit var viewModel: OrderListViewModel
    private lateinit var searchView: SearchView
    private val sharedViewModel: CartViewModel by activityViewModels()
    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private val binding get() = _binding!!
    override fun getViewModel(): OrderListViewModel {
        viewModel = ViewModelProvider(viewModelStore, factory)[OrderListViewModel::class.java]
        return viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val taskAdapter = OrdersAdapter(this)
        binding.apply {
            recyclerViewTasks.apply {
                adapter = taskAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = taskAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTaskSwiped(task)
                }
            }).attachToRecyclerView(recyclerViewTasks)
        }

        setFragmentResultListener("add_edit_request") { _, bundle ->
            val result = bundle.getInt("add_edit_result")
            viewModel.onAddEditResult(result)
        }

        viewModel.orders.observe(viewLifecycleOwner) {
            if(it.isNotEmpty())
                binding.textEmpty.gone()
            else
                binding.textEmpty.visible()
            taskAdapter.submitList(it)
        }

        viewModel.remainingTasks?.observe(viewLifecycleOwner){
            AppLog.debugD("SIZE: ${it.size}")
        }

        binding.layoutViewCart.setOnClickListener {
            (requireActivity() as MainActivity).performFABClick()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tasksEvent.collect { event ->
                    when (event) {
                        is OrderListViewModel.OrdersEvent.ShowUndoDeleteTaskMessage -> {
                            Snackbar.make(requireView(), "Order deleted", Snackbar.LENGTH_LONG)
                                .setAction("UNDO") {
                                    viewModel.onUndoDeleteClick(event.order)
                                }.show()
                        }
                        is OrderListViewModel.OrdersEvent.ShowTaskSavedConfirmationMessage -> {
                            Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                        }
                        is OrderListViewModel.OrdersEvent.NavigateToAddTaskScreen -> {
                            val action =
                                OrderListFragmentDirections.actionOrdersFragmentToAddEditTaskFragment(
                                    "Place New Order",
                                    null
                                )
                            findNavController().navigate(action)

                        }
                        is OrderListViewModel.OrdersEvent.NavigateToEditTaskScreen -> {
                            val action =
                                OrderListFragmentDirections.actionOrdersFragmentToAddEditTaskFragment(
                                    "View Order Details",
                                    event.order
                                )
                            findNavController().navigate(action)
                        }
                        is OrderListViewModel.OrdersEvent.NavigateToDeleteAllCompletedScreen -> {
                            val action =
                                OrderListFragmentDirections.actionGlobalDeleteAllCompletedDialogFragment()
                            findNavController().navigate(action)
                        }
                        else ->{}
                    }.exhaustive
                }
            }
        }
        setHasOptionsMenu(true)
        (requireActivity() as MainActivity).showFAB()
        (requireActivity() as MainActivity).updateFABCart()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value
        if (!pendingQuery.isNullOrEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery, false)
        }

        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_tax_included).isChecked =
                viewModel.preferencesFlow.first().taxIncluded
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_ORDER_NO)
                true
            }
            R.id.action_sort_by_date -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            R.id.action_tax_included -> {
                item.isChecked = !item.isChecked
                viewModel.onHideCompletedClick(item.isChecked)
                true
            }
            R.id.action_printer -> {
                viewModel.onDeleteAllCompletedClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        if(sharedViewModel.shoppingCart.value?.getCartItems()!!.isEmpty()){
            (requireActivity() as MainActivity).updateFABCart()
            binding.layoutViewCart.gone()
        } else {
            (requireActivity() as MainActivity).updateFABCartDetails()
            binding.layoutViewCart.visible()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        searchView.setOnQueryTextListener(null)

    }

    override fun onItemClick(order: Order) {
        viewModel.onTaskSelected(order)
    }

    override fun onItemDeleteClick(order: Order) {
        viewModel.onTaskSwiped(order)
    }

    override fun onItemViewClick(order: Order) {
        val action =
            OrderListFragmentDirections.actionOrdersFragmentToAddEditTaskFragment(
                "View Orders Details",
                order
            )
        findNavController().navigate(action)
    }

    override fun onCheckBoxClick(order: Order, isChecked: Boolean) {
        viewModel.onTaskCheckedChanged(order, isChecked)
    }
}