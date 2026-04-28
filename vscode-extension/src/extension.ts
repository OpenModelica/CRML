import * as path from 'path';
import * as vscode from 'vscode';
import {
    LanguageClient,
    LanguageClientOptions,
    ServerOptions,
    TransportKind,
} from 'vscode-languageclient/node';

let client: LanguageClient;

export function activate(context: vscode.ExtensionContext): void {
    const config = vscode.workspace.getConfiguration('crmlx');

    const configuredJar = config.get<string>('languageServer.jar');
    const jarPath = configuredJar?.trim()
        ? configuredJar
        : path.resolve(
              context.extensionPath,
              '../submodules/language-xtext/build/libs/language-xtext-1.0-SNAPSHOT-ls.jar'
          );

    const extraJvmArgs = config.get<string[]>('languageServer.javaArgs') ?? [];

    const serverOptions: ServerOptions = {
        command: 'java',
        args: [...extraJvmArgs, '-jar', jarPath],
        transport: TransportKind.stdio,
    };

    const clientOptions: LanguageClientOptions = {
        documentSelector: [{ scheme: 'file', language: 'crmlx' }],
        synchronize: {
            fileEvents: vscode.workspace.createFileSystemWatcher('**/*.crmlx'),
        },
    };

    client = new LanguageClient(
        'crmlx',
        'CRML Language Server',
        serverOptions,
        clientOptions
    );

    client.start();
    context.subscriptions.push(client);
}

export function deactivate(): Thenable<void> | undefined {
    return client?.stop();
}
