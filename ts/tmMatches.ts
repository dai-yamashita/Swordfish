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

class TmMatches {

    electron = require('electron');

    container: HTMLDivElement;
    projectId: string;

    tabHolder: TabHolder;
    matches: Map<string, any>;
    origin: HTMLSpanElement;

    constructor(div: HTMLDivElement, projectId: string) {
        this.container = div;
        this.projectId = projectId;
        this.matches = new Map<string, any>();

        let tabContainer: HTMLDivElement = document.createElement('div');
        tabContainer.classList.add('fill_width');
        this.container.appendChild(tabContainer);

        this.tabHolder = new TabHolder(tabContainer, 'tm' + this.projectId);

        let toolbar: HTMLDivElement = document.createElement('div');
        toolbar.classList.add('toolbar');
        toolbar.classList.add('middle');
        this.container.appendChild(toolbar);

        let acceptTranslation = document.createElement('a');
        acceptTranslation.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" height="24" viewBox="0 0 24 24" width="24"><path d="M21 11H6.83l3.58-3.59L9 6l-6 6 6 6 1.41-1.41L6.83 13H21v-2z"/></svg>' +
            '<span class="tooltiptext bottomTooltip">Accept Translation</span>';
        acceptTranslation.className = 'tooltip';
        acceptTranslation.addEventListener('click', () => {
            this.acceptTranslation();
        });
        toolbar.appendChild(acceptTranslation);

        let requestTranslation = document.createElement('a');
        requestTranslation.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" height="24" viewBox="0 0 24 24" width="24"><path d="M12.87 15.07l-2.54-2.51.03-.03c1.74-1.94 2.98-4.17 3.71-6.53H17V4h-7V2H8v2H1v1.99h11.17C11.5 7.92 10.44 9.75 9 11.35 8.07 10.32 7.3 9.19 6.69 8h-2c.73 1.63 1.73 3.17 2.98 4.56l-5.09 5.02L4 19l5-5 3.11 3.11.76-2.04zM18.5 10h-2L12 22h2l1.12-3h4.75L21 22h2l-4.5-12zm-2.62 7l1.62-4.33L19.12 17h-3.24z"/></svg>' +
            '<span class="tooltiptext bottomTooltip">Get Translations from Memory</span>';
        requestTranslation.className = 'tooltip';
        requestTranslation.addEventListener('click', () => {
            this.electron.ipcRenderer.send('search-memory');
        });
        toolbar.appendChild(requestTranslation);

        this.origin = document.createElement('span');
        this.origin.innerText = '';
        this.origin.style.marginTop = '4px';
        this.origin.style.marginLeft = '10px';
        toolbar.appendChild(this.origin);

        this.electron.ipcRenderer.on('accept-tm-match', () => {
            this.acceptTranslation();
        });

        let config: any = { attributes: true, childList: false, subtree: false };
        let observer = new MutationObserver((mutationsList) => {
            for (let mutation of mutationsList) {
                if (mutation.type === 'attributes') {
                    tabContainer.style.height = (this.container.clientHeight - toolbar.clientHeight) + 'px';
                }
            }
        });
        observer.observe(this.container, config);
    }

    clear(): void {
        this.tabHolder.clear();
        this.origin.innerText = '';
        this.matches.clear();
    }

    add(match: any) {
        this.matches.set(match.matchId, match);
        let tab = new Tab(match.matchId, match.similarity + '%', false);

        let height: number = this.container.clientHeight - 65; // tabHolder.labels + toolbar
        let width: number = this.container.clientWidth;
        tab.getContainer().style.height = height + 'px';

        let matchDiv = document.createElement('div');
        matchDiv.style.display = 'flex';
        matchDiv.style.flexDirection = 'column';
        matchDiv.style.height = height + 'px';
        tab.getContainer().appendChild(matchDiv);

        let sourceDiv: HTMLDivElement = document.createElement('div');
        sourceDiv.classList.add('divContainer');
        sourceDiv.classList.add('sourceContainer');
        sourceDiv.classList.add('zoom');
        sourceDiv.innerHTML = match.source;
        let sourceHeight: number = height / 2;
        sourceDiv.style.height = sourceHeight + 'px';
        sourceDiv.style.width = width + 'px';
        matchDiv.appendChild(sourceDiv);

        let targetDiv: HTMLDivElement = document.createElement('div');
        targetDiv.classList.add('divContainer');
        targetDiv.classList.add('targetContainer');
        targetDiv.classList.add('zoom');
        targetDiv.style.height = (height - sourceHeight) + 'px';
        targetDiv.style.width = width + 'px';
        targetDiv.innerHTML = match.target;

        matchDiv.appendChild(targetDiv);

        this.tabHolder.addTab(tab);

        if (this.tabHolder.size() === 1) {
            this.origin.innerText = match.origin;
        }

        tab.getLabel().addEventListener('click', () => {
            this.origin.innerText = match.origin;
        });

        let config: any = { attributes: true, childList: false, subtree: false };
        let observer = new MutationObserver((mutationsList) => {
            for (let mutation of mutationsList) {
                if (mutation.type === 'attributes') {
                    let height = this.container.clientHeight - 65; // tabHolder.labels + toolbar
                    let width: number = this.container.clientWidth;
                    tab.getContainer().style.height = height + 'px';
                    matchDiv.style.height = height + 'px';
                    let sourceHeight: number = height / 2;
                    sourceDiv.style.height = sourceHeight + 'px';
                    sourceDiv.style.width = width + 'px';
                    targetDiv.style.height = (height - sourceHeight) + 'px';
                    targetDiv.style.width = width + 'px';
                }
            }
        });
        observer.observe(this.container, config);
    }

    acceptTranslation(): void {
        if (this.tabHolder.size() === 0) {
            return;
        }
        let selected: string = this.tabHolder.getSelected();
        let match: any = this.matches.get(selected);
        this.electron.ipcRenderer.send('accept-match', match);
    }

    nextMatch(): void {
        this.tabHolder.selectNext();
        let selected: string = this.tabHolder.getSelected();
        let match: any = this.matches.get(selected);
        this.origin.innerText = match.origin;
    }

    previousMatch(): void {
        this.tabHolder.selectPrevious();
        let selected: string = this.tabHolder.getSelected();
        let match: any = this.matches.get(selected);
        this.origin.innerText = match.origin;
    }
}