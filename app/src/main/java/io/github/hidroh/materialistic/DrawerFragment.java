/*
 * Copyright (c) 2015 Ha Duy Trung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.hidroh.materialistic;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;

public class DrawerFragment extends BaseFragment {
    @Inject AlertDialogBuilder mAlertDialogBuilder;
    private TextView mDrawerAccount;
    private View mDrawerLogout;
    private View mDrawerUser;
    private final SharedPreferences.OnSharedPreferenceChangeListener mLoginListener
            = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (getActivity() == null) {
                return;
            }
            if (TextUtils.equals(key, getActivity().getString(R.string.pref_username))) {
                setUsername();
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(mLoginListener);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_drawer, container, false);
        mDrawerAccount = (TextView) view.findViewById(R.id.drawer_account);
        mDrawerAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Account[] accounts = AccountManager.get(getActivity())
                        .getAccountsByType(BuildConfig.APPLICATION_ID);
                if (accounts.length == 0) { // no accounts, ask to login or re-login
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                } else { // has accounts, show account chooser regardless of login status
                    AppUtils.showAccountChooser(getActivity(), mAlertDialogBuilder,
                            accounts);
                }
            }
        });
        mDrawerLogout = view.findViewById(R.id.drawer_logout);
        mDrawerLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialogBuilder
                        .init(getActivity())
                        .setMessage(R.string.logout_confirm)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Preferences.setUsername(getActivity(), null);
                            }
                        })
                        .show();
            }
        });
        mDrawerUser = view.findViewById(R.id.drawer_user);

        view.findViewById(R.id.drawer_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(ListActivity.class);
            }
        });

        view.findViewById(R.id.drawer_popular).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(PopularActivity.class);
            }
        });

        view.findViewById(R.id.drawer_new).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(NewActivity.class);
            }
        });

        view.findViewById(R.id.drawer_show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(ShowActivity.class);
            }
        });

        view.findViewById(R.id.drawer_ask).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(AskActivity.class);
            }
        });

        view.findViewById(R.id.drawer_job).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(JobsActivity.class);
            }
        });

        view.findViewById(R.id.drawer_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(SettingsActivity.class);
            }
        });
        view.findViewById(R.id.drawer_about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(AboutActivity.class);
            }
        });
        view.findViewById(R.id.drawer_favorite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(FavoriteActivity.class);
            }
        });
        view.findViewById(R.id.drawer_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(SubmitActivity.class);
            }
        });
        view.findViewById(R.id.drawer_user).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle extras = new Bundle();
                extras.putString(UserActivity.EXTRA_USERNAME, Preferences.getUsername(getActivity()));
                navigate(UserActivity.class, extras);
            }
        });
        view.findViewById(R.id.drawer_feedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DrawerActivity) getActivity()).showFeedback();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUsername();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .unregisterOnSharedPreferenceChangeListener(mLoginListener);
    }

    private void navigate(Class<? extends Activity> activityClass) {
        navigate(activityClass, null);
    }

    private void navigate(Class<? extends Activity> activityClass, Bundle extras) {
        ((DrawerActivity) getActivity()).navigate(activityClass, extras);
    }

    private void setUsername() {
        if (getView() == null) {
            return;
        }
        String username = Preferences.getUsername(getActivity());
        if (!TextUtils.isEmpty(username)) {
            mDrawerAccount.setText(username);
            mDrawerLogout.setVisibility(View.VISIBLE);
            mDrawerUser.setVisibility(View.VISIBLE);
        } else {
            mDrawerAccount.setText(R.string.login);
            mDrawerLogout.setVisibility(View.GONE);
            mDrawerUser.setVisibility(View.GONE);
        }
    }
}
