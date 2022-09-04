package io.github.wykopmobilny.ui.settings.android

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import io.github.wykopmobilny.ui.settings.SettingsDependencies
import io.github.wykopmobilny.ui.settings.android.databinding.FragmentSettingsBinding
import io.github.wykopmobilny.utils.bindings.bindBackButton
import io.github.wykopmobilny.utils.destroyDependency
import io.github.wykopmobilny.utils.viewBinding

fun preferencesMainFragment(): Fragment = PreferencesMainFragment()

internal class PreferencesMainFragment : Fragment(R.layout.fragment_settings) {

    val binding by viewBinding(FragmentSettingsBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.bindBackButton(activity = activity)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (!childFragmentManager.popBackStackImmediate()) {
                isEnabled = false
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
        }

        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(binding.container.id, GeneralPreferencesFragment())
                .commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().destroyDependency<SettingsDependencies>()
    }
}
