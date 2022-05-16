import { GraphicEntityModule } from './entity-module/GraphicEntityModule.js';
import { TooltipModule } from './tooltip-module/TooltipModule.js';
import { ToggleModule } from './toggle-module/ToggleModule.js'
import { EndScreenModule } from './endscreen-module/EndScreenModule.js';

// List of viewer modules that you want to use in your game
export const modules = [
	GraphicEntityModule,
	TooltipModule,
	ToggleModule,
	EndScreenModule
];

export const gameName = 'Code Keeper - The Hero';

// The list of toggles displayed in the options of the viewer
export const options = [
	ToggleModule.defineToggle({
		// The name of the toggle
		// replace "myToggle" by the name of the toggle you want to use
		toggle: 'toggleFog',
		// The text displayed over the toggle
		title: 'FOG',
		// The labels for the on/off states of your toggle
		values: {
			'SHOW': true,
			'HIDE': false
		},
		// Default value of your toggle
		default: true
	}),
]