module.exports = {
    root: true,
    env: {
        browser: true,
        es2020: true,
        jest: true,
    },
    extends: [
        // https://eslint.org/docs/latest/rules
        'eslint:recommended',
        // https://typescript-eslint.io/users/configs
        'plugin:@typescript-eslint/recommended',
        // https://github.com/jsx-eslint/eslint-plugin-react
        'plugin:react/recommended',
        'plugin:react/jsx-runtime',
        // https://github.com/facebook/react/tree/main/packages/eslint-plugin-react-hooks
        'plugin:react-hooks/recommended',
        // https://github.com/import-js/eslint-plugin-import
        'plugin:import/recommended',
        'plugin:import/typescript',
        // https://github.com/prettier/eslint-config-prettier
        'prettier',
    ],
    ignorePatterns: [
        'build',
        'coverage',
        '.eslintrc.js',
        'tsconfig.json',
        'vite.config.ts',
        'setupTests.ts',
    ],
    parser: '@typescript-eslint/parser',
    parserOptions: {
        project: 'tsconfig.json',
        ecmaVersion: 2020,
        ecmaFeatures: {
            jsx: true,
        },
    },
    plugins: ['@typescript-eslint', 'react', 'react-hooks', 'import', 'jest', 'unused-imports'],
    settings: {
        'import/resolver': {
            node: {
                extensions: ['.js', '.jsx', '.ts', '.tsx'],
                moduleDirectory: ['node_modules', 'src'],
            },
        },
        'import/internal-regex': '^@/',
        react: {
            version: 'detect',
        },
        jest: {
            version: 'detect',
        },
    },
    rules: {
        'no-unused-vars': 'off',
        '@typescript-eslint/no-unused-vars': 'off',
        'unused-imports/no-unused-imports': 'error',
        'unused-imports/no-unused-vars': [
            'warn',
            {
                vars: 'all',
                varsIgnorePattern: '^_',
                args: 'after-used',
                argsIgnorePattern: '^_',
            },
        ],
        'import/order': [
            'error',
            {
                groups: ['builtin', 'external', 'internal', 'parent', 'sibling', 'index'],
                'newlines-between': 'always',
                alphabetize: {
                    order: 'asc',
                    caseInsensitive: true,
                },
                pathGroups: [
                    {
                        pattern: 'react',
                        group: 'external',
                        position: 'before',
                    },
                    {
                        pattern: 'react/**',
                        group: 'external',
                        position: 'before',
                    },
                ],
                pathGroupsExcludedImportTypes: ['react'],
            },
        ],
        'import/newline-after-import': 'error',
        'import/no-duplicates': 'error',
        'import/no-unresolved': 'off',
    },
    overrides: [
        {
            files: ['**/__tests__/**', '**/*.test.tsx'],
            plugins: ['jest', 'jest-dom', 'testing-library'],
            extends: [
                // https://github.com/jest-community/eslint-plugin-jest
                'plugin:jest/recommended',
                // https://github.com/testing-library/eslint-plugin-jest-dom
                'plugin:jest-dom/recommended',
                // https://github.com/testing-library/eslint-plugin-testing-library
                'plugin:testing-library/react',
            ],
            rules: {
                'jest/no-standalone-expect': 'off',
                'testing-library/render-result-naming-convention': 'off',
                'testing-library/no-unnecessary-act': 'off',
                'testing-library/await-async-utils': 'off',
                'testing-library/await-async-events': 'off',
                '@typescript-eslint/require-await': 'off',
            },
        },
    ],
};
