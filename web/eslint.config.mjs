import globals from 'globals';
import js from '@eslint/js';
import tseslint from '@typescript-eslint/eslint-plugin';
import tsParser from '@typescript-eslint/parser';
import react from 'eslint-plugin-react';
import reactHooks from 'eslint-plugin-react-hooks';

/** @type {import('eslint').Linter.Config[]} */
export default [
    {
        ignores: [
            'node_modules/**/*',
            'dist/**/*',
            'build/**/*',
            '*.config.js',
        ],
    },
    {
        files: ['**/*.{js,mjs,cjs,jsx,tsx,ts}'],
        languageOptions: {
            ecmaVersion: 2022,
            sourceType: 'module',
            parser: tsParser,
            parserOptions: {
                ecmaFeatures: {
                    jsx: true,
                },
            },
            globals: {
                ...globals.browser,
                ...globals.es2022,
                IgnitionLib: 'readonly',
                IgnitionReact: 'readonly',
                Redux: 'readonly',
                ReactRedux: 'readonly',
                ReduxThunk: 'readonly',
            },
        },
        plugins: {
            react,
            'react-hooks': reactHooks,
            '@typescript-eslint': tseslint,
        },
        settings: {
            react: {
                version: 'detect',
            },
        },
        rules: {
            ...js.configs.recommended.rules,
            ...tseslint.configs.recommended.rules,
            ...react.configs.recommended.rules,
            ...reactHooks.configs.recommended.rules,

            // TypeScript specific
            '@typescript-eslint/no-unused-vars': ['warn', { argsIgnorePattern: '^_' }],
            '@typescript-eslint/explicit-function-return-type': 'off',
            '@typescript-eslint/no-explicit-any': 'warn',
            '@typescript-eslint/ban-ts-comment': 'warn',

            // React specific
            'react/react-in-jsx-scope': 'off',
            'react/prop-types': 'off',
            'react/display-name': 'off',

            // General
            'no-unused-vars': 'off', // Use TypeScript version instead
            'no-console': 'warn',
        },
    },
];