/*****************************************************************************
Copyright (c) 2007-2021 - Maxprograms,  http://www.maxprograms.com/

Permission is hereby granted, free of charge, to any person obtaining a copy of 
this software and associated documentation files (the "Software"), to compile, 
modify and use the Software in its executable form without restrictions.

Redistribution of this Software or parts of it in any form (source code or 
executable binaries) requires prior written permission from Maxprograms.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
SOFTWARE.
*****************************************************************************/

class GettingStarted {

    electron = require('electron');

    constructor() {
        this.electron.ipcRenderer.send('get-theme');
        this.electron.ipcRenderer.on('set-theme', (event: Electron.IpcRendererEvent, arg: any) => {
            (document.getElementById('theme') as HTMLLinkElement).href = arg;
        });
        document.addEventListener('keydown', (event: KeyboardEvent) => { KeyboardHandler.keyListener(event); });
        document.addEventListener('keydown', (event: KeyboardEvent) => {
            if (event.code === 'Escape') {
                this.electron.ipcRenderer.send('close-getting-started');
            }
        });
        document.getElementById('supportGroup').addEventListener('click', () => { this.electron.ipcRenderer.send('show-support'); });
        document.getElementById('userGuide').addEventListener('click', () => { this.electron.ipcRenderer.send('show-help'); });
        
        let showWindow: HTMLInputElement = document.getElementById('showWindow') as HTMLInputElement;
        showWindow.addEventListener('click', () => {
            this.electron.ipcRenderer.send('show-getting-started', {showGuide: showWindow.checked});
        });
        this.electron.ipcRenderer.send('get-show guide');
        this.electron.ipcRenderer.on('set-show guide', (event: Electron.IpcRendererEvent, arg: any) => {
            showWindow.checked = arg.showGuide;
        });
        let body: HTMLBodyElement = document.getElementById('body') as HTMLBodyElement;
        this.electron.ipcRenderer.send('getting-started-height', { width: body.clientWidth, height: body.clientHeight  });
        document.getElementById('container').focus();
    }
}

new GettingStarted();