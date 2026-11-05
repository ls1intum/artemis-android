Fixtures copied from /Users/krusche/Projects/Apollon/standalone/webapp/tests/fixtures, MIT licensed by TUM Applied Education Technologies.

`deployment-diagram-upstream.json` is the unchanged upstream deployment fixture. `deployment-diagram.json` differs in one value: edge 2f98cc2d-5d0f-41c4-b18c-9a1db6920065 targetHandle is `right` instead of `right-top`, matching Apollon 5.3.1's four cardinal interface handles. No IDs changed.

`legacy-v2-class.json` and `legacy-v3-class.json` are hand-authored compact compatibility fixtures for this wrapper, with stable IDs review-class-person, review-class-course, review-attribute-name and review-enrolls. They verify that official migrations preserve native feedback references.
